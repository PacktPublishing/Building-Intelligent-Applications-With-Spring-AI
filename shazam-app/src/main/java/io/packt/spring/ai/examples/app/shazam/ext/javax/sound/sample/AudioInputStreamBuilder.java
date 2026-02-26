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

import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.cp.elements.lang.ObjectUtils;

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
@SuppressWarnings("unused")
public class AudioInputStreamBuilder implements Builder<AudioInputStream> {

	public static AudioInputStreamBuilder from(Audio audio) {
		return new AudioInputStreamBuilder(BuilderAudioInputStreamSource.from(audio));
	}

	public static AudioInputStreamBuilder from(AudioInputStream audioInputStream) {
		return new AudioInputStreamBuilder(AudioInputStreamSource.from(audioInputStream));
	}

	private final AudioInputStreamSource audioInputStreamSource;

	private AudioFormat audioFormat;

	private Long frameLength;

	protected AudioInputStreamBuilder(AudioInputStreamSource audioInputStreamSource) {
		Assert.notNull(audioInputStreamSource, "AudioInputStreamSource is required");
		this.audioInputStreamSource = audioInputStreamSource;
	}

	protected AudioFormat getAudioFormat() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.audioFormat, getAudioInputStreamSource()::getAudioFormat);
	}

	protected Long getFrameLength() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.frameLength, getAudioInputStreamSource()::getFrameLength);
	}

	public AudioInputStreamBuilder withAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
		return this;
	}

	public AudioInputStreamBuilder withFrameLength(Long frameLength) {
		this.frameLength = frameLength;
		return this;
	}

	public AudioInputStream build() {

		AudioInputStreamSource inputStreamSource = getAudioInputStreamSource();
		AudioFormat audioFormat = getAudioFormat();
		Long frameLength = getFrameLength();

		return newAudioInputStream(inputStreamSource, audioFormat, frameLength);
	}

	private AudioInputStream newAudioInputStream(AudioInputStreamSource inputStreamSource,
			AudioFormat audioFormat, long frameLength) {

		return new AudioInputStream(inputStreamSource.get(), audioFormat, frameLength);
	}

	protected static class BuilderAudioInputStreamSource implements AudioInputStreamSource {

		protected static BuilderAudioInputStreamSource from(Audio audio) {
			return new BuilderAudioInputStreamSource(audio);
		}

		private final AtomicReference<AudioFormat> audioFormat = new AtomicReference<>(null);

		private final AtomicReference<AudioInputStream> audioInputStream = new AtomicReference<>(null);

		private final AtomicReference<Long> frameLength = new AtomicReference<>(null);

		@Getter(AccessLevel.PROTECTED)
		private final Audio audio;

		protected BuilderAudioInputStreamSource(Audio audio) {
			this.audio = AudioUtils.assertAudio(audio);
		}

		@Override
		public AudioInputStream get() {
			return this.audioInputStream.updateAndGet(this::resolveAudioInputStream);
		}

		@Override
		public AudioFormat getAudioFormat() {
			return this.audioFormat.updateAndGet(this::resolveAudioFormat);
		}

		@Override
		public long getFrameLength() {
			return this.frameLength.updateAndGet(this::resolveFrameLength);
		}

		private AudioFormat buildAudioFormat(Audio audio) {
			return AudioFormatBuilder.from(audio)
				.copy(AudioInputStreamSource.super.getAudioFormat())
				.build();
		}

		private Long computeFrameLength(Audio audio) {

			try {
				long audioSize = audio.size();
				long frameSize = getAudioFormat().getFrameSize();
				return audioSize / frameSize;
			}
			catch (Exception ignore) {
				return AudioInputStreamSource.super.getFrameLength();
			}
		}

		private AudioInputStream newAudioInputStream(Audio audio) {
			return AudioUtils.openInputStream(audio);
		}

		private AudioFormat resolveAudioFormat(AudioFormat audioFormat) {
			return audioFormat != null ? audioFormat
				: ConfiguredAudioFormatResolver.INSTANCE.resolve(getAudio(), () -> buildAudioFormat(getAudio()));
		}

		private AudioInputStream resolveAudioInputStream(AudioInputStream audioInputStream) {
			return isValid(audioInputStream) ? audioInputStream : newAudioInputStream(getAudio());
		}

		private boolean isValid(AudioInputStream audioInputStream) {
			return audioInputStream != null && safeAvailable(audioInputStream) > 0;
		}

		private int safeAvailable(AudioInputStream audioInputStream) {
			return ExceptionThrowingSupplier.getSafely(audioInputStream::available, cause -> {
				AudioUtils.close(audioInputStream);
				return 0;
			});
		}

		private long resolveFrameLength(Long framweLength) {
			return framweLength != null ? framweLength : computeFrameLength(getAudio());
		}
	}
}
