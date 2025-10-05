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
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioReadException;
import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;
import io.packt.spring.ai.examples.app.shazam.support.UuidIdGenerator;

import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link AudioSplitter} implementation using the Java Sound API.
 *
 * @author John Blum
 * @see Audio
 * @see AudioProperties
 * @see AudioSplitter
 * @see javax.sound.sampled.AudioSystem
 * @see org.springframework.ai.document.Document
 * @see org.springframework.stereotype.Service
 * @see <a href="https://www.oracle.com/java/technologies/java-sound-api.html">Java Sound API</a>
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class JavaSoundAudioSplitter implements AudioSplitter {

	private final AudioProperties audioProperties;

	@Override
	public List<Document> split(Audio audio) {

		Assert.notNull(audio, "Audio is required");

		try {
			List<Document> documents = new ArrayList<>();

			try (AudioInputStream in = toInputStream(audio)) {

				int bytesRead;
				int audioBufferSize = calculateAudioBufferSize(audio, in);
				byte[] audioBuffer = new byte[audioBufferSize];
				AudioClip previousAudioClip = null;

				while ((bytesRead = in.read(audioBuffer)) > -1) {
					byte[] audioData = getAudioData(audioBuffer, bytesRead, audioBufferSize);
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

	private Document buildDocument(AudioClip audioClip) {

		return Document.builder()
			.idGenerator(UuidIdGenerator.INSTANCE)
			.text(audioClip.encode())
			.build();
	}

	@SuppressWarnings("all")
	private int calculateAudioBufferSize(Audio audio, AudioInputStream in) {
		return calculateAudioBufferSize(audio, in.getFormat());
	}

	private int calculateAudioBufferSize(Audio audio, AudioFormat audioFormat) {

		log("AUDIO FORMAT [%s]%n", audioFormat);

		return AbstractAudioClipper.from(audio)
			.using(getAudioProperties())
			.in(audioFormat)
			.build()
			.calculateAudioClipSizeInBytes();
	}

	private byte[] getAudioData(byte[] audioBuffer, int bytesRead, int chunkSize) {

		byte[] audioData = audioBuffer;

		if (bytesRead != chunkSize) {
			audioData = new byte[bytesRead];
			System.arraycopy(audioBuffer, 0, audioData, 0, bytesRead);
		}

		return audioData;
	}

	private void log(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	private AudioInputStream toInputStream(Audio audio) throws IOException, UnsupportedAudioFileException {
		InputStream in = audio.toResource().getInputStream();
		return AudioSystem.getAudioInputStream(in);
	}

	record AudioClip(Audio audio) {

		static AudioClip from(byte[] audioData) {
			return new AudioClip(Audio.from(audioData));
		}

		byte[] data() {
			return audio().getData();
		}

		byte[] dataCopy(int position, int length) {
			byte[] dataCopy = new byte[length];
			System.arraycopy(data(), position, dataCopy, 0, length);
			return dataCopy;
		}

		String encode() {
			return audio().encode();
		}

		AudioClip firstHalf() {
			int length = size() / 2;
			byte[] audioData = dataCopy(0, length);
			return AudioClip.from(audioData);
		}

		AudioClip merge(AudioClip audioClip) {
			byte[] audioData = new byte[size() + audioClip.size()];
			System.arraycopy(data(), 0, audioData, 0, size());
			System.arraycopy(audioClip.data(), 0, audioData, size(), audioClip.size());
			return AudioClip.from(audioData);
		}

		AudioClip secondHalf() {
			int size = size();
			int halfSize = size / 2;
			int length = size - halfSize;
			byte[] audioData = dataCopy(halfSize, length);
			return AudioClip.from(audioData);
		}

		int size() {
			return audio().size();
		}
	}

	@FunctionalInterface
	@SuppressWarnings("unused")
	interface AudioClipper {

		int BITS_PER_BYTE = 8;
		int DEFAULT_COMPRESSION_RATIO = 1; // measured as ?:1, for example 10:1 (10 to 1); 1:1 is no compression
		int HUMAN_HEARING_FREQUENCY = 20_000; // 20,000 Hz (20 kHz)
		int MONO_CHANNEL = 1;
		int MIN_SAMPLE_SIZE_IN_BITS = 8; // 1 byte
		int NYQUIST_FREQUENCY = 22_050; // 22.05 kHz
		int STEREO_CHANNEL = 2;

		int calculateAudioClipSizeInBytes();

	}

	@SuppressWarnings("unused")
	@Getter(AccessLevel.PROTECTED)
	static abstract class AbstractAudioClipper implements AudioClipper {

		static AbstractAudioClipper.Builder from(Audio audio) {
			return new Builder(audio);
		}

		private final Audio audio;

		private final AudioFormat audioFormat;

		private final AudioProperties audioProperties;

		AbstractAudioClipper(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {

			Assert.notNull(audio, "Audio is required");
			Assert.notNull(audioFormat, "AudioFormat is required");
			Assert.notNull(audioProperties, "AudioProperties are required");

			this.audio = audio;
			this.audioFormat = audioFormat;
			this.audioProperties = audioProperties;
		}

		protected int getAudioClipLengthInSeconds() {
			Duration audioClipLength = getAudioProperties().getClipLength();
			return Long.valueOf(audioClipLength.toSeconds()).intValue();
		}

		// Audio data size in bytes
		protected int getAudioSize() {
			return getAudio().getData().length;
		}

		protected int getAudioSizeInBits() {
			return getAudioSize() * BITS_PER_BYTE;
		}

		// Number of Bits per Second (measured in kilobits per second (kbps))
		// For example, if 128 kbps, then returns 128,000
		protected int getBitRate() {

			int bitRate = AudioSystem.NOT_SPECIFIED;

			Audio audio = getAudio();
			Duration audioDuration = audio.getDuration();

			if (TimeUtils.isNotZero(audioDuration)) {
				int audioSizeInBytes = audio.size();
				int audioSizeInBits = audioSizeInBytes * BITS_PER_BYTE;
				int seconds = Long.valueOf(audioDuration.getSeconds()).intValue();
				bitRate = audioSizeInBits / seconds;
			}

			return bitRate;
		}

		protected int getBitResolution() {
			return getBitRate() / getSampleRate();
		}

		// 1 for Mono / 2 for Stereo
		protected int getChannels() {
			return Math.max(MONO_CHANNEL, getAudioFormat().getChannels());
		}

		protected int getCompressionRatio() {
			return DEFAULT_COMPRESSION_RATIO;
		}

		// Number of Samples / Second (measured in Hertz (Hz) or Kilohertz (kHz))
		protected int getSampleRate() {
			float audioFormatSampleRate = getAudioFormat().getSampleRate();
			return Float.valueOf(audioFormatSampleRate).intValue();
		}

		// Number of Bits / Sample
		protected int getSampleSizeInBits() {
			int audioFormatSampleSize = getAudioFormat().getSampleSizeInBits();
			return isSpecified(audioFormatSampleSize) ? audioFormatSampleSize : getBitResolution();
		}

		// Number of Channels * Number of Bits / Sample
		protected int getTotalSampleSizeInBits() {
			return getChannels() * getSampleSizeInBits();
		}

		protected double getTotalSampleSizeInBytes() {
			double totalSimpleSizeInBits = getTotalSampleSizeInBits();
			return getTotalSampleSizeInBits() / (double) BITS_PER_BYTE;
		}

		protected boolean inStereo() {
			return getChannels() == STEREO_CHANNEL;
		}

		protected boolean isSpecified(int audioValue) {
			return Math.max(audioValue, AudioSystem.NOT_SPECIFIED) > 0;
		}

		protected int bytesPerSecond() {
			int bytesPerSecond = Long.valueOf(Math.round(getTotalSampleSizeInBytes() * getSampleRate())).intValue();
			return  bytesPerSecond / getCompressionRatio();
		}

		@Override
		public int calculateAudioClipSizeInBytes() {
			int sampleSizeInBytesPerSecond = bytesPerSecond();
			int seconds = getAudioClipLengthInSeconds();
			return sampleSizeInBytesPerSecond * seconds;
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
				return getAudioFormat().toString().toLowerCase().contains(MPEG);
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

			AbstractAudioClipper build() {
				return isMpegAudioFormat()
					? new MpegLayer3AudioClipper(getAudio(), getAudioFormat(), getAudioProperties())
					: new CompactDiscAudioClipper(getAudio(), getAudioFormat(), getAudioProperties());
			}
		}
	}

	// CD
	static class CompactDiscAudioClipper extends AbstractAudioClipper {

		static final int CD_SAMPLE_RATE = 44_100; // 44,100 Hz (44.1 kHz); 44,100 samples per second
		static final int CD_SAMPLE_SIZE_IN_BITS = 16; // AKA Bit Depth or Bit Resolution

		// 44,100 samples per second * 16 bits per sample * 2 channels (stereo) uncompressed
		// 1,411,200 bits per second, 1411.2 kbps
		static final int CD_BIT_RATE = CD_SAMPLE_RATE * CD_SAMPLE_SIZE_IN_BITS * STEREO_CHANNEL;

		CompactDiscAudioClipper(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
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
	@SuppressWarnings("unused")
	static class MpegLayer3AudioClipper extends AbstractAudioClipper {

		static final int MP3_BIT_RATE_STANDARD_QUALITY = 128_000; // 128 kbps (128,000 bits per second)
		static final int MP3_BIT_RATE_HIGH_QUALITY = 320_000; // 320 kbps; low compression
		static final int MP3_BIT_RATE_LOW_QUALITY = 64_000; // 64 kbps; high compression
		static final int MP3_BIT_RATE = MP3_BIT_RATE_STANDARD_QUALITY;
		static final int MP3_SAMPLE_RATE = 22_050;
		static final int MP3_SAMPLE_SIZE_IN_BITS =
			Math.round((float) MP3_BIT_RATE_STANDARD_QUALITY / (float) MP3_SAMPLE_RATE);

		MpegLayer3AudioClipper(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
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
