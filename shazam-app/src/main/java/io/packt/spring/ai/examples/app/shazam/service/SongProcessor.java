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
package io.packt.spring.ai.examples.app.shazam.service;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import io.packt.spring.ai.examples.app.shazam.model.Song;

import org.springframework.ai.document.Document;

/**
 * Strategy interface for processing {@link Song Songs}.
 *
 * @author John Blum
 * @see Song
 * @see FunctionalInterface
 * @see org.springframework.ai.document.Document
 * @see java.util.function.BiFunction
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface SongProcessor extends BiFunction<Song, List<Document>, List<Document>> {

	@Override
	default List<Document> apply(Song song, List<Document> documents) {
		return process(song, documents);
	}

	default List<Document> process(Song song) {
		return process(song, Collections.emptyList());
	}

	List<Document> process(Song song, List<Document> documents);

	default SongProcessor andThen(BiFunction<Song, List<Document>, List<Document>> songProcessor) {

		return songProcessor == null ? this : (song, documents) ->
			songProcessor.apply(song, process(song, documents));
	}
}
