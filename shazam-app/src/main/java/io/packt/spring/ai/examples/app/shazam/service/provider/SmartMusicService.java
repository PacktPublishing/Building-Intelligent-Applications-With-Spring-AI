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

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;
import io.packt.spring.ai.examples.app.shazam.support.SongNotFoundException;
import io.packt.spring.ai.examples.app.shazam.support.UuidIdGenerator;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link MusicService} implementation using AI.
 *
 * @author John Blum
 * @see MusicService
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class SmartMusicService implements MusicService {

	private final AudioSplitter audioSplitter;

	private final VectorStore vectorStore;

	@Override
	public Song search(Audio audio) {

		Assert.notNull(audio, "Audio is required");

		SearchRequest searchRequest = buildSearchRequest(audio);
		List<Document> songMatches = getVectorStore().similaritySearch(searchRequest);

		// TODO: Lookup Song in database based on matching Documents

		throw new SongNotFoundException("No song was found with the given audio");
	}

	private SearchRequest buildSearchRequest(Audio audio) {

		return SearchRequest.builder()
			.query(audio.encode())
			.build();
	}

	@Override
	public void store(Song song) {

		Assert.notNull(song, "Song is required");

		List<Document> documents = getAudioSplitter().split(song);
		List<Document> identifiedDocuments = documents.stream().map(this::identify).toList();

		getVectorStore().accept(identifiedDocuments);
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
