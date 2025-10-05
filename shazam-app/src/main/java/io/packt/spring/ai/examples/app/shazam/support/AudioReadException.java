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

import io.packt.spring.ai.examples.app.shazam.model.Audio;

/**
 * Java {@link RuntimeException} thrown when trying to read {@link Audio} data.
 *
 * @author John Blum
 * @see RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class AudioReadException extends RuntimeException {

	public static AudioReadException because(String message, Throwable cause) {
		return new AudioReadException(message, cause);
	}

	public AudioReadException() {

	}

	public AudioReadException(String message) {
		super(message);
	}

	public AudioReadException(Throwable cause) {
		super(cause);
	}

	public AudioReadException(String message, Throwable cause) {
		super(message, cause);
	}
}
