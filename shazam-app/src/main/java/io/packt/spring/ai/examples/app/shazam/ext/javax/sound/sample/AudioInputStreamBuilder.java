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

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for {@link AudioInputStream}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioInputStream
 * @see org.cp.elements.lang.Builder
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class AudioInputStreamBuilder implements Builder<AudioInputStream> {

	public static AudioInputStreamBuilder from(Audio audio) {
		return new AudioInputStreamBuilder(audio);
	}

	private final Audio audio;

	protected AudioInputStreamBuilder(Audio audio) {
		Assert.notNull(audio, "Audio is required");
		this.audio = audio;
	}

	public AudioInputStream build() {

		Audio audio = getAudio();

		try (AudioInputStream audioInputStream = AudioUtils.openInputStream(audio)) {
			long frameLength = audioInputStream.getFrameLength();
			AudioFormat audioFormat = buildAudioFormat(audio, audioInputStream);
			return new AudioInputStream(audio.inputStream(), audioFormat, frameLength);
		}
		catch (IOException cause) {
			throw new RuntimeException("Failed to open InputStream to Audio", cause);
		}
	}

	protected AudioFormat buildAudioFormat(Audio audio, AudioInputStream audioInputStream) {
		return AudioFormatBuilder.from(audio)
			.copyFormatFrom(audioInputStream)
			.build();
	}
}
