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
package io.packt.spring.ai.examples.app.shazam.support;

import java.io.File;

import io.packt.spring.ai.examples.app.shazam.model.Song;

/**
 * Java {@link RuntimeException} thrown while loading a {@link Song}.
 *
 * @author John Blum
 * @see Song
 * @see java.lang.RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class SongLoadException extends RuntimeException {

	public static SongLoadException from(File song, Throwable cause) {
		String message = "Failed to load song from file [%s]".formatted(song.getAbsolutePath());
		return new SongLoadException(message, cause);
	}

	public SongLoadException() {

	}

	public SongLoadException(String message) {
		super(message);
	}

	public SongLoadException(Throwable cause) {
		super(cause);
	}

	public SongLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
