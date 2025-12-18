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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for an {@link AudioInputStream}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioInputStream
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class AudioInputStreamBuilder {

	public static AudioInputStreamBuilder from(Audio audio) {
		return new AudioInputStreamBuilder(audio);
	}

	private final Audio audio;

	AudioInputStreamBuilder(Audio audio) {
		Assert.notNull(audio, "Audio is required");
		this.audio = audio;
	}

	protected AudioInputStream getAudioInputStream(Audio audio) {
		return ExceptionThrowingSupplier.getSafely(() ->
			AudioSystem.getAudioInputStream(audio.inputStream()));
	}

	protected AudioFormat resolveAudioFormat(Audio audio, AudioInputStream audioInputStream) {
		return AudioFormatBuilder.from(audio)
			.with(audioInputStream)
			.build();
	}

	public AudioInputStream build() {

		Audio audio = getAudio();
		AudioInputStream audioInputStream = getAudioInputStream(audio);
		AudioFormat resolvedAudioFormat = resolveAudioFormat(audio, audioInputStream);

		return new AudioInputStream(audio.inputStream(), resolvedAudioFormat, audioInputStream.getFrameLength());
	}
}
