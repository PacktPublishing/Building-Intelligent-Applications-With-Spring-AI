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
package io.packt.spring.ai.examples.app.shazam.service.provider;

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioReadException;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;
import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioSplitter} implementation using the Java Sound API.
 *
 * @author John Blum
 * @see Audio
 * @see AudioProperties
 * @see AudioSplitter
 * @see java.time.Duration
 * @see javax.sound.sampled.AudioSystem
 * @see org.springframework.ai.document.Document
 * @see org.springframework.stereotype.Service
 * @see <a href="https://www.oracle.com/java/technologies/java-sound-api.html">Java Sound API</a>
 * @since 0.1.0
 */
@Service
@Getter(AccessLevel.PROTECTED)
public class JavaSoundAudioSplitter extends AbstractAudioSplitter {

	public JavaSoundAudioSplitter(AudioProperties audioProperties) {
		super(audioProperties);
	}

	@Override
	public List<Document> split(Audio audio) {

		Assert.notNull(audio, "Audio is required");

		try {
			List<Document> documents = new ArrayList<>();

			try (AudioInputStream in = toInputStream(audio)) {
				AudioClip previousAudioClip = null;
				AudioFormat audioFormat = log(in.getFormat());
				int audioBufferSize = calculateAudioBufferSize(audio, audioFormat);
				byte[] audioBuffer = new byte[audioBufferSize];

				for (int bytesRead = in.read(audioBuffer); bytesRead > -1; bytesRead = in.read(audioBuffer)) {
					byte[] audioData = copyAudioData(audioBuffer, bytesRead);
					AudioClip audioClip = AudioClip.from(audioData);
					Document document = buildDocument(audioClip);
					documents.add(document);

					if (previousAudioClip != null) {
						AudioClip overlappingAudioClip = previousAudioClip.secondHalf().merge(audioClip.firstHalf());
						Document overlappingDocument = buildDocument(overlappingAudioClip);
						documents.add(overlappingDocument);
					}

					previousAudioClip = audioClip;
				}
			}

			return documents;
		}
		catch (Exception cause) {
			throw AudioReadException.because("Failed to read audio", cause);
		}
	}

	private int calculateAudioBufferSize(Audio audio, AudioFormat audioFormat) {

		return AbstractAudioClipCalculator.from(audio)
			.using(getAudioProperties())
			.in(audioFormat)
			.build()
			.calculateAudioClipSizeInBytes();
	}

	private byte[] copyAudioData(byte[] audioBuffer, int bytesRead) {

		byte[] audioData = audioBuffer;

		if (bytesRead != audioBuffer.length) {
			audioData = new byte[bytesRead];
			System.arraycopy(audioBuffer, 0, audioData, 0, bytesRead);
		}

		return audioData;
	}

	private AudioFormat log(AudioFormat audioFormat) {
		getLogger().info("Audio Format [{}]", audioFormat);
		return audioFormat;
	}

	private AudioInputStream toInputStream(Audio audio) throws IOException, UnsupportedAudioFileException {
		InputStream in = audio.toResource().getInputStream();
		return AudioSystem.getAudioInputStream(in);
	}

	/**
	 * {@link FunctionalInterface} defining a contract to clip {@link Audio} by size or time.
	 */
	@FunctionalInterface
	@SuppressWarnings("unused")
	interface AudioClipCalculator {

		int BITS_PER_BYTE = NumberUtils.BITS_PER_BYTE;
		int DEFAULT_COMPRESSION_RATIO = 1; // measured as ?:1, for example 10:1 (10 to 1); 1:1 is no compression
		int HUMAN_HEARING_FREQUENCY = 20_000; // 20,000 Hz (20 kHz)
		int NYQUIST_FREQUENCY = 22_050; // 22.05 kHz

		int calculateAudioClipSizeInBytes();

	}

	@SuppressWarnings("unused")
	@Getter(AccessLevel.PROTECTED)
	static abstract class AbstractAudioClipCalculator implements AudioClipCalculator {

		static AbstractAudioClipCalculator.Builder from(Audio audio) {
			return new Builder(audio);
		}

		private final Audio audio;

		private final AudioFormat audioFormat;

		private final AudioProperties audioProperties;

		AbstractAudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {

			Assert.notNull(audio, "Audio is required");
			Assert.notNull(audioFormat, "AudioFormat is required");
			Assert.notNull(audioProperties, "AudioProperties are required");

			this.audio = audio;
			this.audioFormat = audioFormat;
			this.audioProperties = audioProperties;
		}

		protected int getAudioClipLengthInSeconds() {
			Duration audioClipLength = getAudioProperties().getClipLength();
			long seconds = audioClipLength.toSeconds();
			return asInt(seconds);
		}

		protected AudioChannels getAudioChannels() {
			return AudioChannels.from(getAudioFormat());
		}

		protected Duration getAudioDuration() {
			return getAudio().getDuration();
		}

		protected int getAudioSizeInBytes() {
			return getAudio().getData().length;
		}

		protected int getAudioSizeInBits() {
			return getAudioSizeInBytes() * BITS_PER_BYTE;
		}

		// Number of Bits per Second (measured in kilobits per second (kbps))
		// For example, if 128 kbps, then returns 128,000
		protected int getBitRate() {

			Duration audioDuration = getAudioDuration();

			if (TimeUtils.isNotZero(audioDuration)) {
				int audioSizeInBits = getAudioSizeInBits();
				int seconds = asInt(audioDuration.getSeconds());
				return audioSizeInBits / seconds;
			}

			return AudioSystem.NOT_SPECIFIED;
		}

		// AKA Bit Depth
		protected int getBitResolution() {
			return getBitRate() / getSampleRate();
		}

		protected int getCompressionRatio() {
			return DEFAULT_COMPRESSION_RATIO;
		}

		// Number of Samples / Second (measured in Hertz (Hz) or Kilohertz (kHz))
		protected int getSampleRate() {
			float audioFormatSampleRate = getAudioFormat().getSampleRate();
			return asInt(audioFormatSampleRate);
		}

		// Number of Bits / Sample
		protected int getSampleSizeInBits() {
			int audioFormatSampleSize = getAudioFormat().getSampleSizeInBits();
			return isSpecified(audioFormatSampleSize) ? audioFormatSampleSize : getBitResolution();
		}

		// Number of AudioChannels * Number of Bits / Sample
		protected int getTotalSampleSizeInBits() {
			return getAudioChannels().value() * getSampleSizeInBits();
		}

		// AKA Bit Rate
		protected int bitsPerSecond() {
			return getTotalSampleSizeInBits() * getSampleRate();
		}

		protected int bytesPerSecond() {
			int bitsPerSecond = bitsPerSecond();
			int bytesPerSecond = bitsPerSecond / BITS_PER_BYTE;
			return bytesPerSecond / getCompressionRatio();
		}

		@Override
		public int calculateAudioClipSizeInBytes() {
			int bytesPerSecond = bytesPerSecond();
			int seconds = getAudioClipLengthInSeconds();
			return bytesPerSecond * seconds;
		}

		protected boolean isSpecified(int audioValue) {
			return Math.max(audioValue, AudioSystem.NOT_SPECIFIED) > 0;
		}

		@Getter(AccessLevel.PROTECTED)
		static class Builder {

			private static final Duration FIVE_SECONDS = Duration.ofSeconds(5);

			private static final String MPEG = "mpeg";

			private final Audio audio;

			private AudioFormat audioFormat;

			private AudioProperties audioProperties;

			Builder(Audio audio) {
				Assert.notNull(audio, "Audio is required");
				this.audio = audio;
			}

			private boolean isMpegAudioFormat() {
				return describe(getAudioFormat()).contains(MPEG);
			}

			private String describe(AudioFormat audioFormat) {
				return getAudioFormat().toString().toLowerCase();
			}

			private AudioProperties defaultAudioProperties() {
				AudioProperties audioProperties = new AudioProperties();
				audioProperties.setClipLength(FIVE_SECONDS);
				return audioProperties;
			}

			private AudioProperties resolveAudioProperties(AudioProperties audioProperties) {
				return audioProperties != null ? audioProperties : defaultAudioProperties();
			}

			Builder in(AudioFormat audioFormat) {
				Assert.notNull(audioFormat, "AudioFormat is required");
				this.audioFormat = audioFormat;
				return this;
			}

			Builder using(AudioProperties audioProperties) {
				this.audioProperties = resolveAudioProperties(audioProperties);
				return this;
			}

			AbstractAudioClipCalculator build() {
				return isMpegAudioFormat()
					? new MpegLayer3AudioClipCalculator(getAudio(), getAudioFormat(), getAudioProperties())
					: new CompactDiscAudioClipCalculator(getAudio(), getAudioFormat(), getAudioProperties());
			}
		}
	}

	@SuppressWarnings("unused")
	enum AudioChannels {

		MONO(1), STEREO(2);

		static AudioChannels from(int value) {

			return switch (value) {
				case 1 -> MONO;
				case 2 -> STEREO;
				default -> {
					String message = "[%d] is not a valid AudioChannels".formatted(value);
					throw new IllegalArgumentException(message);
				}
			};
		}

		static AudioChannels from(AudioFormat audioFormat) {
			try {
				return from(audioFormat.getChannels());
			}
			catch (Exception ignore) {
				return MONO;
			}
		}

		private final int value;

		AudioChannels(int value) {
			this.value = value;
		}

		public boolean inStereo() {
			return this.equals(STEREO);
		}

		public int value() {
			return this.value;
		}
	}

	// CD
	static class CompactDiscAudioClipCalculator extends AbstractAudioClipCalculator implements CompactDiscMetadata {

		// 44,100 samples per second * 16 bits per sample * 2 channels (stereo) uncompressed
		// 1,411,200 bits per second, 1411.2 kbps
		static final int CD_BIT_RATE = CD_SAMPLE_RATE * CD_SAMPLE_SIZE_IN_BITS * AudioChannels.STEREO.value();

		CompactDiscAudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			super(audio, audioFormat, audioProperties);
		}

		@Override
		protected int getBitRate() {
			int bitRate = super.getBitRate();
			return isSpecified(bitRate) ? bitRate : CD_BIT_RATE;
		}

		@Override
		protected int getSampleRate() {
			int sampleRate = super.getSampleRate();
			return isSpecified(sampleRate) ? sampleRate : CD_SAMPLE_RATE;
		}

		@Override
		protected int getSampleSizeInBits() {
			int sampleSize = super.getSampleSizeInBits();
			return isSpecified(sampleSize) ? sampleSize : CD_SAMPLE_SIZE_IN_BITS;
		}
	}

	// MP3
	static class MpegLayer3AudioClipCalculator extends AbstractAudioClipCalculator implements MpegLayer3Metadata {

		MpegLayer3AudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			super(audio, audioFormat, audioProperties);
		}

		@Override
		protected int getBitRate() {
			int bitRate = super.getBitRate();
			return isSpecified(bitRate) ? bitRate : MP3_BIT_RATE;
		}

		@Override
		protected int getSampleRate() {
			int sampleRate = super.getSampleRate();
			return isSpecified(sampleRate) ? sampleRate : MP3_SAMPLE_RATE;
		}

		@Override
		protected int getSampleSizeInBits() {
			int sampleSize = super.getSampleSizeInBits();
			return isSpecified(sampleSize) ? sampleSize : MP3_SAMPLE_SIZE_IN_BITS;
		}
	}
}
