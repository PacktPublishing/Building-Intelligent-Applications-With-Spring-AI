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

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.io.IOException;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Builder;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for {@link AudioInputStream}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFormat
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

	private Long frameLength;

	protected AudioInputStreamBuilder(Audio audio) {
		this.audio = AudioUtils.assertAudio(audio);
	}

	protected @Nullable AudioFormat getAudioFormat() {
		return ConfiguredAudioFormatResolver.INSTANCE.resolve(getAudio());
	}

	@SuppressWarnings("unused")
	public AudioInputStreamBuilder withFrameLength(Long frameLength) {
		this.frameLength = frameLength;
		return this;
	}

	public AudioInputStream build() {

		Audio audio = getAudio();
		AudioFormat audioFormat = getAudioFormat();
		Long frameLength = getFrameLength();

		return audioFormat != null && isFrameLengthSpecified(frameLength)
			? newAudioInputStream(audio, audioFormat, frameLength)
			: buildAudioInputStream(audio);
	}

	private AudioInputStream newAudioInputStream(Audio audio, AudioFormat audioFormat, long frameLength) {
		return new AudioInputStream(audio.inputStream(), audioFormat, frameLength);
	}

	private AudioInputStream buildAudioInputStream(Audio audio) {

		AudioInputStream audioInputStream = null;

		try (AudioInputStream in = AudioUtils.openInputStream(audio)) {
			AudioFormat audioFormat = buildAudioFormat(audio, in);
			long frameLength = resolveFrameLength(resolveFrameLength(getFrameLength(), in::getFrameLength),
				() -> computeFrameLength(audio, audioFormat));
			audioInputStream = newAudioInputStream(audio, audioFormat, frameLength);
		}
		catch (IOException ignore) {
			// IOException thrown when closing intermediate AudioInputStream
		}

		return audioInputStream;
	}

	private AudioFormat buildAudioFormat(Audio audio, AudioInputStream audioInputStream) {
		return AudioFormatBuilder.from(audio)
			.copyAudioFormat(audioInputStream)
			.build();
	}

	private boolean isFrameLengthSpecified(Long frameLength) {
		return frameLength != null && AudioUtils.isSpecified(asInt(frameLength));
	}

	private long computeFrameLength(Audio audio, AudioFormat audioFormat) {
		long audioSize = audio.size();
		long frameSize = audioFormat.getFrameSize();
		return audioSize / frameSize;
	}

	private Long resolveFrameLength(Long frameLength, Supplier<Long> defaultFrameLength) {
		return isFrameLengthSpecified(frameLength) ? frameLength : defaultFrameLength.get();
	}
}
