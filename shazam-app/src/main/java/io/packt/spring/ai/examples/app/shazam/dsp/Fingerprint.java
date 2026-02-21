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
 * Abstract Data Type (ADT) modeling a {@literal digital fingerprint}, such as for audio data.
 *
 * @author John Blum
 * @param <T> {@link Class Data type} of the {@literal fingerprint}.
 * @see FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface Fingerprint<T> {

	T get();

	default byte[] getData() {
		throw new UnsupportedOperationException("Getting a byte array from the fingerprint is not supported");
	}

	default String toHexString() {

		StringBuilder stringBuilder = new StringBuilder();

		for (byte dataByte : getData()) {
			stringBuilder.append(Integer.toHexString(Byte.toUnsignedInt(dataByte)));
		}

		return stringBuilder.toString();
	}
}
