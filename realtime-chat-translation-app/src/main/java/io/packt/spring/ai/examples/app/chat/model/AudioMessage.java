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
package io.packt.spring.ai.examples.app.chat.model;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling audio (sound) data.
 *
 * @author John Blum
 * @param data {@link byte[]} containing the data of the audio (sound);
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record AudioMessage(byte[] data) {

	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public static AudioMessage from(byte[] data) {
		Assert.notNull(data, "Audio bytes is required");
		return new AudioMessage(data);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	public int size() {
		return data().length;
	}
}
