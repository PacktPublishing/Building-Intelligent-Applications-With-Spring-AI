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
import java.util.stream.Collectors;

import io.packt.spring.ai.examples.app.shazam.config.SongSearchProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;
import io.packt.spring.ai.examples.app.shazam.support.SongNotFoundException;
import io.packt.spring.ai.examples.app.shazam.support.UuidIdGenerator;

import org.slf4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MusicService} implementation using AI.
 *
 * @author John Blum
 * @see MusicService
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

		SearchRequest searchRequest = buildSearchRequest(audio);
		List<Document> songMatches = getVectorStore().similaritySearch(searchRequest);
		Set<UUID> songIds = resolveSongIdentifiers(songMatches);

		if (!songIds.isEmpty()) {
			// TODO: Lookup Song in database based on matching Documents
		}

		throw new SongNotFoundException("No song was found with the given audio");
	}

	private SearchRequest buildSearchRequest(Audio audio) {

		return SearchRequest.builder()
			.similarityThreshold(getSongSearchProperties().resolveSimilarityThreshold())
			.topK(getSongSearchProperties().resolveTopK())
			.query(audio.encode())
			.build();
	}

	private UUID assertSongIdentifier(Object songId) {
		Assert.isInstanceOf(UUID.class, songId, () -> "Expected Song ID [%s] to be a UUID".formatted(songId));
		return (UUID) songId;
	}

	private <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	private UUID resolveSongIdentifier(Document document) {
		return assertSongIdentifier(document.getMetadata().get(SONG_ID_KEY));
	}

	private Set<UUID> resolveSongIdentifiers(List<Document> songMatches) {
		return nullSafeList(songMatches).stream()
			.map(this::resolveSongIdentifier)
			.collect(Collectors.toSet());
	}

	@Override
	public void store(Song song) {

		Assert.notNull(song, "Song is required");

		List<Document> documents = getAudioSplitter().split(song);

		List<Document> identifiedDocuments = documents.stream()
			.map(document -> associateSong(document, song))
			.map(this::identify)
			.toList();

		getVectorStore().accept(identifiedDocuments);

		// TODO: Store Song in database
	}

	private Document associateSong(Document document, Song song) {
		document.getMetadata().put(SONG_ID_KEY, song.getId());
		return document;
	}

	private boolean isIdentified(Document document) {
		return StringUtils.hasText(document.getId());
	}

	private Document identify(Document document) {
		return isIdentified(document) ? document : copy(document);
	}

	private Document copy(Document document) {

		return Document.builder()
			.idGenerator(UuidIdGenerator.INSTANCE)
			.media(document.getMedia())
			.score(document.getScore())
			.text(document.getText())
			.build();
	}
}
