/*
 * Copyright 2024 Author or Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.packt.spring.ai.examples.similaritysearch;

import static io.codeprimate.extensions.util.ExceptionThrowingFunction.doSafely;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.codeprimate.extensions.spring.ai.document.EmbeddedDocument;
import io.codeprimate.extensions.spring.ai.embedding.EmbeddingModelWrapper;
import io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter;
import io.codeprimate.extensions.spring.ai.transformer.splitter.NewlineTextSplitter;
import io.codeprimate.extensions.spring.ai.transformer.splitter.ParagraphTextSplitter;
import io.codeprimate.extensions.spring.ai.vectorstore.DecoratedSimpleVectorStore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal nomic-embed-text} model)
 * to demonstrate Similarity Search.
 *
 * @author John Blum
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.transformer.splitter.TextSplitter
 * @see org.springframework.ai.vectorstore.SearchRequest
 * @see org.springframework.ai.vectorstore.SimpleVectorStore
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@Profile("!test")
@SuppressWarnings("unused")
public class SongSimilaritySearchApplication {

	private static final boolean SHOW_ALL = false;

	private static final double SIMILARITY_THRESHOLD = 0.70d;

	private static final int TOP_K = 1_000;

	private static final Predicate<String> NON_ESSENTIAL_SONG_WORDS_PREDICATE =
		word -> !Set.of("be", "do", "to").contains(word);

	private static final String USER_PROFILE = "user";

	private static final String[] SONG_JSON_FILES = {
		"acdc-backinblack.json",
		"beegees-stayinalive.json",
		"pearljam-alive.json",
		"pearljam-black.json",
	};

	public static void main(String[] args) {

		new SpringApplicationBuilder(SongSimilaritySearchApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	DecoratedSimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
		return new DecoratedSimpleVectorStore(embeddingModel);
	}

	@Bean
	@Profile("!vector-similarity")
	ApplicationRunner programRunner(DecoratedSimpleVectorStore vectorStore, ObjectMapper objectMapper) {

		return args -> {

			loadSongs(objectMapper, embeddingFunction(vectorStore));

			print("Using Similarity Threshold [%s]%n", SIMILARITY_THRESHOLD);
			print("Using Top K [%d]%n", TOP_K);

			List.of("alive", "back in black", "do I deserve to be").forEach(query -> {

				SearchRequest searchRequest = newSearchRequest(query)
					.similarityThreshold(SIMILARITY_THRESHOLD)
					.topK(TOP_K)
					.build();

				List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

				//print("Similar Documents %s%n", similarDocuments);

				List<Song> similarSongs = similarDocuments.stream()
					.map(Song::from)
					.distinct()
					.sorted()
					.toList();

				print("%nSongs similar to [\"%s\"]: %s%n", searchRequest.getQuery(), similarSongs);
			});
		};
	}

	@Bean
	@Profile("vector-similarity")
	ApplicationRunner vectorSimilarity(ObjectMapper objectMapper, EmbeddingModel embeddingModel) {

		return args -> {

			DocumentTextSplitter textSplitter = new DocumentTextSplitter();

			String query = "alive";
			//String query = "back in black";
			//String query = "do I deserve to be";
			//String query = "\"You're still alive,\" she said, oh, and do I deserve to be?";

			print("Using Query [%s]%n", query);
			print("Using Similarity Threshold [%s]%n", SIMILARITY_THRESHOLD);
			print("Using Top K [%d]%n", TOP_K);

			float[] queryEmbedding = embeddingModel.embed(textSplitter.preProcess(query));

			loadSongs(objectMapper, embeddingFunction(embeddingModel)).forEach(document -> {

				float[] documentEmbedding = document.getEmbedding();

				double cosineSimilarity = SimpleVectorStore.EmbeddingMath
					.cosineSimilarity(documentEmbedding, queryEmbedding);

				if (SHOW_ALL || cosineSimilarity >= SIMILARITY_THRESHOLD) {
					print("Document [%s] Content [%s] compare to Query [%s] has Cosine Similarity: %s%n%n",
						document.getId(), document.getText(), query, cosineSimilarity);
				}
			});
		};
	}

	protected Function<Document, EmbeddedDocument> embeddingFunction(DecoratedSimpleVectorStore vectorStore) {

		return document -> {
			vectorStore.add(document);
			return vectorStore.get(document.getId());
		};
	}

	protected Function<Document, EmbeddedDocument> embeddingFunction(EmbeddingModel embeddingModel) {
		return EmbeddingModelWrapper.from(embeddingModel)::embedThenReturn;
	}

	protected List<EmbeddedDocument> loadSongs(ObjectMapper objectMapper,
			Function<Document, EmbeddedDocument> embeddingFunction) {

		return Arrays.stream(SONG_JSON_FILES)
			.filter(songPredicate())
			.map(ClassPathResource::new)
			.map(doSafely(ClassPathResource::getContentAsByteArray))
			.map(doSafely(byteArray -> objectMapper.readValue(byteArray, Song.class)))
			.map(this::logSong)
			.map(Song::toChunkedDocuments)
			.flatMap(List::stream)
			.map(embeddingFunction)
			.toList();
	}

	private Song logSong(Song song) {
		print("Loaded Song [%s]%n", song);
		logSongByArtistAndTitle(song, "NA", "NA");
		return song;
	}

	@SuppressWarnings("all")
	private void logSongByArtistAndTitle(Song song, String artist, String title) {
		if (Song.ARTIST_PREDICATE.test(song, artist) && Song.TITLE_PREDICATE.test(song, title)) {
			song.toChunkedDocuments().forEach(document -> print("Document for Song [%s]: [%s]%n%n",
				document.getId(), document.getText()));
		}
	}

	protected SearchRequest.Builder newSearchRequest(String query) {
		return new SearchRequest.Builder().query(query);
	}

	protected void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	@SuppressWarnings("all")
	protected Predicate<String> songPredicate() {

		Predicate<String> songPredicate = song -> true;

		Predicate<String> songArtistPredicate = IntStream.range(0, SONG_JSON_FILES.length)
		//Predicate<String> songArtistPredicate =  IntStream.of(1, 2)
			.mapToObj(index -> SONG_JSON_FILES[index])
			.<Predicate<String>>map(song -> song::equalsIgnoreCase)
			.reduce(Predicate::or)
			.orElseGet(() -> song -> false);

		return songPredicate.and(songArtistPredicate);
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Song implements Comparable<Song> {

		protected static final BiPredicate<Song, String> ARTIST_PREDICATE = (song, artist) ->
			song.getArtist().equalsIgnoreCase(artist);

		protected static final BiPredicate<Song, String> TITLE_PREDICATE = (song, title) ->
			song.getTitle().equalsIgnoreCase(title);

		protected static final String BY_REGEX = "\\bby\\b";
		protected static final String POUND = "#";
		protected static final String ID_COUNT_TEMPLATE = "%s" + POUND + "%d";
		protected static final String SONG_TO_STRING = "%s by %s";

		public static Song from(Document document) {

			Assert.notNull(document, "Document is required");

			String id = document.getId();
			String resolvedId = stripCount(id);

			String[] titleAndArtist = resolvedId.split(BY_REGEX);

			return Song.builder()
				.artist(titleAndArtist[1].trim())
				.title(titleAndArtist[0].trim())
				.lyrics(document.getText())
				.build();
		}

		private static String stripCount(String value) {
			int index = String.valueOf(value).indexOf(POUND);
			return index > -1 ? value.substring(0, index) : value;
		}

		private final AtomicInteger documentCounter = new AtomicInteger(0);

		private String artist;
		private String lyrics;
		private String title;

		@Getter(AccessLevel.PROTECTED)
		private final Set<TextSplitter> textSplitters = Set.of(
			//new TokenTextSplitter(12, 80, 5, 10_000, false)
			//new SongLyricsTextSplitter(),
			new SongRefrainTextSplitter(),
			new SongVerseTextSplitter()
		);

		public String getId() {
			return toString();
		}

		protected String getIdWithCount() {
			return ID_COUNT_TEMPLATE.formatted(getId(), getDocumentCounter().incrementAndGet());
		}

		public List<Document> toChunkedDocuments() {

			List<Document> chunkedDocuments = new ArrayList<>();

			for (TextSplitter textSplitter : getTextSplitters()) {

				List<Document> documents = textSplitter.split(toDocument()).stream()
					.filter(document -> StringUtils.hasText(document.getText()))
					.map(document -> buildDocument(getIdWithCount(), document.getText()))
					.toList();

				chunkedDocuments.addAll(documents);
			}

			return chunkedDocuments;
		}

		public Document toDocument() {
			return buildDocument(getId(), getLyrics());
		}

		private Document buildDocument(String id, String content) {

			return Document.builder()
				.text(content)
				.id(id)
				.build();
		}

		@Override
		public int compareTo(Song song) {

			int value = this.getArtist().compareTo(song.getArtist());

			return value == 0
				? this.getTitle().compareTo(song.getTitle())
				: value;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Song that)) {
				return false;
			}

			return this.getArtist().equals(that.getArtist())
				&& this.getTitle().equals(that.getTitle());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getArtist(), getTitle());
		}

		@Override
		public String toString() {
			return SONG_TO_STRING.formatted(getTitle(), getArtist());
		}
	}

	static class SongLyricsTextSplitter extends DocumentTextSplitter {

		@Override
		protected Predicate<String> nonEssentialWordsPredicate() {
			return NON_ESSENTIAL_SONG_WORDS_PREDICATE;
		}
	}

	static class SongRefrainTextSplitter extends NewlineTextSplitter {

		@Override
		protected Predicate<String> nonEssentialWordsPredicate() {
			return NON_ESSENTIAL_SONG_WORDS_PREDICATE;
		}
	}

	static class SongVerseTextSplitter extends ParagraphTextSplitter {

		@Override
		protected Predicate<String> nonEssentialWordsPredicate() {
			return NON_ESSENTIAL_SONG_WORDS_PREDICATE;
		}
	}
}
