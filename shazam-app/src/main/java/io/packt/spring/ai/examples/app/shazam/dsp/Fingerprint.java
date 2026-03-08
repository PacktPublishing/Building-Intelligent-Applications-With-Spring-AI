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

import java.util.function.Consumer;

/**
 * Abstract Data Type (ADT) modeling a {@literal digital fingerprint},
 * such as {@literal fingerprint} computed for audio data.
 *
 * @author John Blum
 * @param <T> {@link Class Type} of the {@literal fingerprint} {@literal data}.
 * @see FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface Fingerprint<T> {

	/**
	 * Gets the underlying typed {@link T data} constituting this digital {@link Fingerprint}.
	 *
	 * @return the underlying {@link T data} constituting this {@link Fingerprint}.
	 * @see #getData()
	 */
	T get();

	/**
	 * Gets the binary data constituting this digital {@link Fingerprint}.
	 * <p>
	 * The digital {@link Fingerprint} may be a {@literal hash} or sequence of {@literal hashes}.
	 *
	 * @return the binary data constituting this digital {@link Fingerprint}.
	 * @throws UnsupportedOperationException by default; the binary representation is type dependent.
	 * @see #get()
	 */
	default byte[] getData() {
		throw new UnsupportedOperationException("Getting a byte array from the digital fingerprint is not supported");
	}

	/**
	 * Function accepting a {@link Consumer} to process the raw {@literal digital fingerprint data}.
	 *
	 * @param consumer {@link Consumer} used to process the raw {@literal digital fingerprint data}.
	 * @see java.util.function.Consumer
	 */
	default void consume(Consumer<T> consumer) {

	}

	/**
	 * Returns a {@literal hexidecimal repesentation} of this digital {@link Fingerprint}.
	 *
	 * @return a {@literal hexidecimal repesentation} of this digital {@link Fingerprint}.
	 * @throws UnsupportedOperationException if this digital {@link Fingerprint} does not have a binary form.
	 * @see #getData()
	 */
	default String toHexString() {

		StringBuilder stringBuilder = new StringBuilder();

		for (byte dataByte : getData()) {
			stringBuilder.append(Integer.toHexString(Byte.toUnsignedInt(dataByte)));
		}

		return stringBuilder.toString();
	}
}
