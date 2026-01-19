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
package io.packt.spring.ai.examples.app.shazam.service.provider;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.codeprimate.extensions.util.ImmutableSetWrapper;
import io.packt.spring.ai.examples.app.shazam.config.SongSearchProperties;
import io.packt.spring.ai.examples.app.shazam.ext.spring.ai.vectorstore.AudioSearchRequest;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.repo.SongRepository;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.DocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;
import io.packt.spring.ai.examples.app.shazam.support.NonUniqueSongException;
import io.packt.spring.ai.examples.app.shazam.support.SongNotFoundException;
import io.packt.spring.ai.examples.app.shazam.support.UuidGenerator;

import org.slf4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MusicService} implementation using AI.
 *
 * @author John Blum
 * @see Audio
 * @see Song
 * @see AudioSplitter
 * @see DocumentStore
 * @see MusicService
 * @see SongRepository
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class SmartMusicService implements MusicService {

	private static final String SONG_ID_KEY = "songId";

	private final AudioSplitter audioSplitter;

	private final DocumentStore documentStore;

	private final SongRepository songRepository;

	private final SongSearchProperties songSearchProperties;

	private final VectorStore vectorStore;

	protected Logger getLogger() {
		return log;
	}

	@PostConstruct
	public void afterInit() {
		getLogger().info("Using VectorStore [{}]", getVectorStore().getClass().getName());
	}

	@Override
	public Song search(Audio audio) {

		Assert.notNull(audio, "Audio is required");

		AudioSearchRequest searchRequest = buildSearchRequest(audio);
		Document document = searchRequest.toDocument();

		try {
			getDocumentStore().save(document);

			List<Document> matchingSongs = search(searchRequest);
			ImmutableSetWrapper<UUID> songIdentifiers = identifySongs(matchingSongs);

			if (songIdentifiers.isNotEmpty()) {

				UUID matchingSongIdentifier = ExceptionThrowingSupplier.getSafely(songIdentifiers::onlyOne, cause -> {
					throw NonUniqueSongException.with(songIdentifiers, cause);
				});

				return getSongRepository().findById(matchingSongIdentifier).orElseThrow(() ->
					SongNotFoundException.from(matchingSongIdentifier));
			}

			throw SongNotFoundException.because("No song was found for the given audio");
		}
		finally {
			getDocumentStore().remove(document);
		}
	}

	private AudioSearchRequest buildSearchRequest(Audio audio) {

		SearchRequest searchRequest = SearchRequest.builder()
			.similarityThreshold(getSongSearchProperties().resolveSimilarityThreshold())
			.topK(getSongSearchProperties().resolveTopK())
			.query(UuidGenerator.INSTANCE.generateId())
			.build();

		return AudioSearchRequest.builder(searchRequest)
			.query(audio)
			.build();
	}

	private List<Document> search(SearchRequest searchRequest) {
		return getVectorStore().similaritySearch(searchRequest);
	}

	private UUID assertSongIdentifier(Object songId) {
		Assert.isInstanceOf(UUID.class, songId, () -> "Expected Song ID [%s] to be a UUID".formatted(songId));
		return (UUID) songId;
	}

	private ImmutableSetWrapper<UUID> identifySongs(List<Document> songMatches) {

		Set<UUID> songIdentifiers = nullSafeList(songMatches).stream()
			.map(this::resolveSongIdentifier)
			.collect(Collectors.toSet());

		return ImmutableSetWrapper.from(songIdentifiers);
	}

	private <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	private UUID resolveSongIdentifier(Document document) {
		return resolveSongIdentifier(document.getMetadata().get(SONG_ID_KEY));
	}

	private UUID resolveSongIdentifier(Object songId) {
		return songId instanceof String stringSongId ? UUID.fromString(songId.toString())
			: assertSongIdentifier(songId);
	}

	@Override
	public void store(Song song, BiFunction<Song, List<Document>, List<Document>> songProcessor) {

		Assert.notNull(song, "Song is required");
		Assert.notNull(songProcessor, "Song processor is required");

		List<Document> documents = getAudioSplitter().split(song);
		List<Document> identifiedDocuments = identify(documents, song);

		identifiedDocuments = songProcessor.apply(song, identifiedDocuments);

		try {
			getVectorStore().accept(identifiedDocuments);
			getSongRepository().save(song);
		}
		finally {
			identifiedDocuments.forEach(getDocumentStore()::remove);
		}
	}

	private List<Document> identify(List<Document> documents, Song song) {

		return documents.stream()
			.map(document -> associateSong(document, song))
			.map(getDocumentStore()::save)
			.toList();
	}

	private Document associateSong(Document document, Song song) {
		document.getMetadata().put(SONG_ID_KEY, song.getId());
		return document;
	}
}
