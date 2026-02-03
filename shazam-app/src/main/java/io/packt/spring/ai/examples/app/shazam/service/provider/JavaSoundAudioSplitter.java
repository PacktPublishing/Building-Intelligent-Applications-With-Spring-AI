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

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.BITS_PER_BYTE;
import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asFloat;
import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioChannels;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;
import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;

import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioSplitter} implementation using the Java Sound API.
 *
 * @author John Blum
 * @see Audio
 * @see AudioChannels
 * @see AudioProperties
 * @see AudioSplitter
 * @see java.time.Duration
 * @see javax.sound.sampled.AudioFormat
 * @see javax.sound.sampled.AudioSystem
 * @see org.springframework.ai.document.Document
 * @see org.springframework.stereotype.Service
 * @see <a href="https://www.oracle.com/java/technologies/java-sound-api.html">Java Sound API</a>
 * @since 0.1.0
 */
@Service
@Profile("AudioTime")
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

			try (AudioInputStream in = openInputStream(audio)) {

				AudioFormat audioFormat = in.getFormat();
				AudioClip previousAudioClip = null;

				int audioBufferSize = calculateAudioBufferSize(audio, audioFormat);
				byte[] audioBuffer = new byte[audioBufferSize];

				for (int bytesRead = in.read(audioBuffer); bytesRead > -1; bytesRead = in.read(audioBuffer)) {

					byte[] audioData = copyAudioData(audioBuffer, bytesRead);
					AudioClip audioClip = AudioClip.from(audioData, audioFormat);
					Document audioDocument = buildDocument(audioClip, false);

					if (previousAudioClip != null) {
						AudioClip overlappingAudioClip = previousAudioClip.secondHalf().merge(audioClip.firstHalf());
						Document overlappingAudioDocument = buildDocument(overlappingAudioClip, true);
						documents.add(overlappingAudioDocument);
					}

					previousAudioClip = audioClip;
					documents.add(audioDocument);
				}
			}

			return documents;
		}
		catch (Exception cause) {
			throw AudioAccessException.because("Failed to read audio", cause);
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

	private AudioInputStream openInputStream(Audio audio) {
		return AudioInputStreamBuilder.from(audio).build();
	}

	/**
	 * {@link FunctionalInterface} defining a contract to clip {@link Audio} by size or time.
	 */
	@FunctionalInterface
	@SuppressWarnings("unused")
	public interface AudioClipCalculator {

		int DEFAULT_COMPRESSION_RATIO = 1; // measured as ?:1, for example 10:1 (10 to 1); 1:1 is no compression
		int HUMAN_HEARING_FREQUENCY = 20_000; // 20,000 Hz (20 kHz)
		int NYQUIST_FREQUENCY = 22_050; // 22.05 kHz

		int calculateAudioClipSizeInBytes();

	}

	@SuppressWarnings("unused")
	@Getter(AccessLevel.PROTECTED)
	protected static abstract class AbstractAudioClipCalculator implements AudioClipCalculator {

		static AbstractAudioClipCalculator.Builder from(Audio audio) {
			return new Builder(audio);
		}

		private final Audio audio;

		private final AudioFormat audioFormat;

		private final AudioProperties audioProperties;

		protected AbstractAudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			this.audio = AudioUtils.assertAudio(audio);
			this.audioFormat = AudioUtils.assertAudioFormat(audioFormat);
			this.audioProperties = AudioProperties.nullSafe(audioProperties);
		}

		protected double getAudioClipDurationInSeconds() {
			Duration audioClipDuration = getAudioProperties().getClipDuration();
			long audioClipDurationInMilliseconds = audioClipDuration.toMillis();
			return audioClipDurationInMilliseconds / 1000.0d;
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

		// Number of bits per second (measured in kilobits per second (kbps))
		// For example, if 128 kbps, then returns 128,000 bits in 1 second
		// AKA Bit Depth | Bit Resolution
		protected int getBitRate() {

			// If bit rate is known, return it
			if (getAudioFormat() instanceof ShazamAudioFormat shazamAudioFormat) {
				Integer bitRate = shazamAudioFormat.getBitRate();
				if (bitRate != null) {
					return bitRate;
				}
			}

			// Compute bit rate as number of samples per second multiplied by the size of a sample in bits
			if (isSampleRateSpecified() && isSampleSizeSpecified()) {
				int sampleRate = getSampleRate();
				int sampleSize = getTotalSampleSizeInBits();
				return sampleRate * sampleSize;
			}

			Duration audioDuration = getAudioDuration();

			// Compute bit rate as size of audio in bits divided by the duration of audio in seconds
			if (TimeUtils.isNotZero(audioDuration)) {
				float audioSizeInBits = getAudioSizeInBits();
				float seconds = asInt(audioDuration.getSeconds());
				return Math.round(audioSizeInBits / seconds);
			}

			return AudioUtils.unspecified();
		}

		protected int getCompressionRatio() {
			return DEFAULT_COMPRESSION_RATIO;
		}

		protected boolean isSampleRateSpecified() {
			return AudioUtils.isSpecified(getSampleRate());
		}

		// Number of Samples / Second (measured in Hertz (Hz) or Kilohertz (kHz))
		protected int getSampleRate() {
			float sampleRate = getAudioFormat().getSampleRate();
			return asInt(sampleRate);
		}

		protected boolean isSampleSizeSpecified() {
			return AudioUtils.isSpecified(getSampleSizeInBits());
		}

		// Number of Bits / Sample
		protected int getSampleSizeInBits() {
			return getAudioFormat().getSampleSizeInBits();
		}

		// Number of Bits / Sample * Number of Audio Channels
		protected int getTotalSampleSizeInBits() {
			return getSampleSizeInBits() * getAudioChannels().value();
		}

		// @see getBitRate()
		protected int bitsPerSecond() {
			return getBitRate();
		}

		@SuppressWarnings("all")
		protected int bytesPerSecond() {
			int bitsPerSecond = bitsPerSecond();
			int bytesPerSecond = Math.round(asFloat(bitsPerSecond) / BITS_PER_BYTE);
			int compressedBytesPerSecond = bytesPerSecond / getCompressionRatio();
			return compressedBytesPerSecond;
		}

		@Override
		public int calculateAudioClipSizeInBytes() {
			int bytesPerSecond = bytesPerSecond();
			double seconds = getAudioClipDurationInSeconds();
			return asInt(Math.round(bytesPerSecond * seconds));
		}

		@Getter(AccessLevel.PROTECTED)
		static class Builder {

			private static final String MPEG = "mpeg";

			private final Audio audio;

			private AudioFormat audioFormat;

			private AudioProperties audioProperties;

			Builder(Audio audio) {
				this.audio = AudioUtils.assertAudio(audio);
			}

			Builder in(AudioFormat audioFormat) {
				this.audioFormat = AudioUtils.assertAudioFormat(audioFormat);
				return this;
			}

			Builder using(AudioProperties audioProperties) {
				this.audioProperties = AudioProperties.nullSafe(audioProperties);
				return this;
			}

			private boolean isMpegAudioFormat() {
				return describe(getAudioFormat()).contains(MPEG);
			}

			private String describe(AudioFormat audioFormat) {
				return getAudioFormat().getEncoding().toString().toLowerCase();
			}

			AbstractAudioClipCalculator build() {

				return isMpegAudioFormat()
					? new MpegAudioClipCalculator(getAudio(), getAudioFormat(), getAudioProperties())
					: new WavAudioClipCalculator(getAudio(), getAudioFormat(), getAudioProperties());
			}
		}
	}

	// MP3
	static class MpegAudioClipCalculator extends AbstractAudioClipCalculator implements MpegMetadata {

		MpegAudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			super(audio, audioFormat, audioProperties);
		}

		@Override
		protected int getBitRate() {
			int bitRate = super.getBitRate();
			return AudioUtils.isSpecified(bitRate) ? bitRate
				: getAudioProperties().getMp3BitRate(MP3_BIT_RATE);
		}

		@Override
		protected int getSampleRate() {
			int sampleRate = super.getSampleRate();
			return AudioUtils.isSpecified(sampleRate) ? sampleRate
				: getAudioProperties().getMp3SampleRate(MP3_SAMPLE_RATE);
		}
	}

	// Wave (WAVE) as recorded on Compact Disc (CD)
	static class WavAudioClipCalculator extends AbstractAudioClipCalculator implements CompactDiscMetadata {

		// 44,100 samples per second * 16 bits per sample * 2 channels (stereo) uncompressed
		// 1,411,200 bits per second, 1411.2 kbps
		static final int CD_BIT_RATE = CD_SAMPLE_RATE * CD_SAMPLE_SIZE_IN_BITS * AudioChannels.STEREO.value();

		WavAudioClipCalculator(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			super(audio, audioFormat, audioProperties);
		}

		@Override
		protected int getBitRate() {
			int bitRate = super.getBitRate();
			return AudioUtils.isSpecified(bitRate) ? bitRate : CD_BIT_RATE;
		}

		@Override
		protected int getSampleRate() {
			int sampleRate = super.getSampleRate();
			return AudioUtils.isSpecified(sampleRate) ? sampleRate : CD_SAMPLE_RATE;
		}

		@Override
		protected int getSampleSizeInBits() {
			int sampleSize = super.getSampleSizeInBits();
			return AudioUtils.isSpecified(sampleSize) ? sampleSize : CD_SAMPLE_SIZE_IN_BITS;
		}
	}
}
