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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal nomic-embed-text} model)
 * to demonstrate Similarity Search.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class SongSimilaritySearchApplication {

	private static final double SIMILARITY_THRESHOLD = 0.55d;

	private static final String[] SONG_JSON_FILES = {
		"acdc-backinblack.json",
		"beegees-stayinalive.json",
		"pearljam-alive.json",
		"pearljam-black.json",
	};

	public static void main(String[] args) {

		new SpringApplicationBuilder(SongSimilaritySearchApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return new SimpleVectorStore(embeddingModel);
	}

	@Bean
	ApplicationRunner programRunner(VectorStore vectorStore, ObjectMapper objectMapper) {

		return args -> {

			loadSongLyrics(vectorStore, objectMapper);

			Set.of("alive", "back in black").forEach(query -> {

				SearchRequest searchRequest = SearchRequest.query(query)
					.withSimilarityThreshold(SIMILARITY_THRESHOLD);

				List<Document> similarSongs = vectorStore.similaritySearch(searchRequest);

				System.out.printf("%nSongs similar to [\"%s\"]: %s%n", searchRequest.getQuery(),
					similarSongs.stream().map(Document::getId).toList());
			});
		};
	}

	private void loadSongLyrics(VectorStore vectorStore, ObjectMapper objectMapper) {

		List<Document> documents = Arrays.stream(SONG_JSON_FILES)
			.map(ClassPathResource::new)
			.map(doSafely(ClassPathResource::getContentAsByteArray))
			.map(doSafely(byteArray -> objectMapper.readValue(byteArray, Song.class)))
			.map(this::logSong)
			.map(Song::toChunkedDocuments)
			.flatMap(List::stream)
			.toList();

		vectorStore.accept(documents);
	}

	private Song logSong(Song song) {
		System.out.printf("Loading Song [%s]%n", song);
		return song;
	}

	private static <S, T> Function<S, T> doSafely(ExceptionThrowingFunction<S, T> function) {

		return target -> {
			try {
				return function.apply(target);
			}
			catch (Exception cause) {
				throw new RuntimeException(cause);
			}
		};
	}

	interface ExceptionThrowingFunction<S, T> {
		T apply(S target) throws Exception;
	}

	@Getter
	static class Song {

		protected static final String SONG_STRING = "%s by %s";

		private String artist;
		private String lyrics;
		private String title;

		@Getter(AccessLevel.PROTECTED)
		private final TextSplitter textSplitter = new TokenTextSplitter(12, 80, 5, 10_000, false);

		public String getId() {
			return toString();
		}

		public List<Document> toChunkedDocuments() {

			return getTextSplitter().split(toDocument()).stream()
				.map(document -> buildDocument(getId(), document.getContent()))
				.toList();
		}

		public Document toDocument() {
			return buildDocument(getId(), getLyrics());
		}

		private Document buildDocument(String id, String content) {

			return Document.builder()
				.withContent(content)
				.withId(id)
				.build();
		}

		@Override
		public String toString() {
			return SONG_STRING.formatted(getTitle(), getArtist());
		}
	}
}
