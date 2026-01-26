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
package io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample;

import javax.sound.sampled.AudioFormat;

/**
 * {@link Enum Enumeration} of {@link AudioFormat#getChannels() audio channels}.
 *
 * @author John Blum
 * @see AudioFormat#getChannels()
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum AudioChannels {

	MONO(1), STEREO(2);

	public static AudioChannels from(int value) {

		return switch (value) {
			case 1 -> MONO;
			case 2 -> STEREO;
			default -> {
				String message = "[%d] is not valid number of audio channels".formatted(value);
				throw new IllegalArgumentException(message);
			}
		};
	}

	public static AudioChannels from(AudioFormat audioFormat) {

		try {
			return from(audioFormat.getChannels());
		}
		catch (Exception ignore) {
			return MONO;
		}
	}

	private final int value;

	AudioChannels(int value) {
		this.value = Math.max(value, 1);
	}

	public boolean isMono() {
		return this.equals(MONO);
	}

	public boolean inStereo() {
		return this.equals(STEREO);
	}

	public int value() {
		return this.value;
	}
}
