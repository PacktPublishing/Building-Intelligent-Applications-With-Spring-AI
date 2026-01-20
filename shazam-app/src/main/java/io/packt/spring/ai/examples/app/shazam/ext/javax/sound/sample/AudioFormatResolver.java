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

import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

/**
 * Strategy interface defining a contract to resolve the {@link AudioFormat} of given {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see java.lang.FunctionalInterface
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AudioFormatResolver {

	static AudioFormatResolver defaultAudioFormatResolver() {
		return new DefaultAudioFormatResolver();
	}

	default AudioFormat resolve(Audio audio) {
		return resolve(audio, () -> null);
	}

	default AudioFormat resolve(Audio audio, AudioFormat defaultAudioFormat) {
		return resolve(audio, () -> defaultAudioFormat);
	}

	AudioFormat resolve(Audio audio, Supplier<AudioFormat> defaultAudioFormat);

}
