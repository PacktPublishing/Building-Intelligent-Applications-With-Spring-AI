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
package io.packt.spring.ai.examples.app.shazam.dsp;

/**
 * Java {@link RuntimeException} thrown when computing an {@literal audio fingerprint}.
 *
 * @author John Blum
 * @see RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class AudioFingerprintException extends RuntimeException {

	public static AudioFingerprintException because(String message, Throwable cause) {
		return new AudioFingerprintException(message, cause);
	}

	public AudioFingerprintException() {

	}

	public AudioFingerprintException(String message) {
		super(message);
	}

	public AudioFingerprintException(Throwable cause) {
		super(cause);
	}

	public AudioFingerprintException(String message, Throwable cause) {
		super(message, cause);
	}
}
