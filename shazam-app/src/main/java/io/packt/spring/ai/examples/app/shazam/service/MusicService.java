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

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;

/**
 * Interface defining a service for processing music.
 *
 * @author John Blum
 * @see Audio
 * @see Song
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface MusicService {

	/**
	 * Searches for a {@link Song} matching the given {@link Audio}.
	 *
	 * @param audio {@link Audio} data used to search for and find a matching {@link Song}.
	 * @return a {@link Song} matching the given {@link Audio}.
	 * @see Audio
	 * @see Song
	 */
	Song search(Audio audio);

	/**
	 * Stores the given {@link Song} and its {@link Audio} data in the database.
	 *
	 * @param song {@link Song} to store.
	 * @see Song
	 */
	void store(Song song);

}
