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
package io.packt.spring.ai.examples.app.shazam.service;

import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.BITS_PER_BYTE;
import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.asFloat;
import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.asInt;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioChannels;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.AudioSource;
import io.packt.spring.ai.examples.app.shazam.model.MediaSource;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;

import org.slf4j.Logger;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class encapsulating functionality common to all {@link AudioSplitter} implementations.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSplitter
 * @see AudioProperties
 * @see javax.sound.sampled.AudioFormat
 * @see org.springframework.ai.content.Media
 * @see org.springframework.ai.document.Document
 * @see org.springframework.beans.factory.InitializingBean
 * @since 0.1.0
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public abstract class AbstractAudioSplitter implements AudioSplitter, InitializingBean {

	public static final String AUDIO_BYTE_OFFSET_KEY = "byteOffset";
	public static final String AUDIO_CLIP_OVERLAP_KEY = "overlap";
	public static final String AUDIO_TIMESTAMP_KEY = "timestamp";

	private final AudioProperties audioProperties;

	public AbstractAudioSplitter(AudioProperties audioProperties) {
		Assert.notNull(audioProperties, "AudioProperties are required");
		this.audioProperties = audioProperties;
	}

	@Override
	public void afterPropertiesSet() {
		getLogger().info("Using AudioSplitter [{}]", this);
	}

	protected Logger getLogger() {
		return log;
	}

	protected Document buildDocument(AudioClip audioClip) {
		return buildDocument(audioClip, false);
	}

	protected Document buildDocument(AudioClip audioClip, boolean overlap) {

		Map<String, Object> audioClipMetadata = Map.of(
			AUDIO_BYTE_OFFSET_KEY, audioClip.getByteOffset(),
			AUDIO_CLIP_OVERLAP_KEY, overlap,
			AUDIO_TIMESTAMP_KEY, audioClip.getTimestamp()
		);

		return AbstractDocumentStore.newAudioDocument(audioClip, audioClipMetadata);
	}

	private byte[] copyAudioData(byte[] audioBuffer, int bytesRead) {

		Assert.state(bytesRead <= audioBuffer.length, "Number of bytes read [%d] cannot exceed audio buffer size [%d]"
			.formatted(bytesRead, audioBuffer.length));

		byte[] audioData = audioBuffer;

		if (bytesRead != audioBuffer.length) {
			audioData = new byte[bytesRead];
			System.arraycopy(audioBuffer, 0, audioData, 0, bytesRead);
		}

		return audioData;
	}

	protected byte[] newAudioBuffer(Audio audio, AudioFormat audioFormat) {

		return AbstractAudioBufferFactory.from(audio)
			.in(audioFormat)
			.using(getAudioProperties())
			.build()
			.newAudioBuffer();
	}

	protected int readAudioData(AudioInputStream in, byte[] audioBuffer) throws IOException {
		return in.read(audioBuffer);
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	/**
	 * Abstract Data Type (ADT) and Java Record modeling a clip of {@link Audio} data.
	 *
	 * @param audio {@link Audio} clip
	 * @see Audio
	 * @see AudioSource
	 * @see MediaSource
	 */
	@SuppressWarnings("unused")
	public record AudioClip(Audio audio, AtomicLong timestamp, AtomicInteger byteOffset)
			implements AudioSource, MediaSource {

		private static final MimeType AUDIO_MPEG = new MimeType("audio", "mpeg");
		private static final MimeType AUDIO_WAVE = new MimeType("audio", "wav");

		public AudioClip {
			AudioUtils.assertAudio(audio);
		}

		public static AudioClip from(byte[] audioData, AudioFormat audioFormat) {
			Audio audio = Audio.from(audioData).in(audioFormat);
			return new AudioClip(audio, new AtomicLong(0L), new AtomicInteger(0));
		}

		@Override
		public Audio getAudio() {
			return audio();
		}

		public int getByteOffset() {
			return byteOffset().get();
		}

		public byte[] getData() {
			return audio().getData();
		}

		public AudioFormat getFormat() {
			return getAudio().getFormat();
		}

		@Override
		public Media getMedia() {
			return new Media(AUDIO_WAVE, audio().resource());
		}

		public long getTimestamp() {
			return timestamp().get();
		}

		public AudioClip atTimestamp(long timestamp) {
			this.timestamp.set(timestamp);
			return this;
		}

		public AudioClip fromByteOffset(int byteOffset) {
			this.byteOffset.set(byteOffset);
			return this;
		}

		private byte[] dataCopy(int position, int length) {
			byte[] dataCopy = new byte[length];
			System.arraycopy(getData(), position, dataCopy, 0, length);
			return dataCopy;
		}

		public AudioClip firstHalf() {
			int length = asInt(size() / 2L);
			byte[] audioData = dataCopy(0, length);
			return AudioClip.from(audioData, getFormat());
		}

		public AudioClip merge(AudioClip thatAudioClip) {
			int thisAudioClipLength = asInt(size());
			int thatAudioClipLength = asInt(thatAudioClip.size());
			int audioDataLength = thisAudioClipLength + thatAudioClipLength;
			byte[] audioData = new byte[audioDataLength];
			System.arraycopy(getData(), 0, audioData, 0, thisAudioClipLength);
			System.arraycopy(thatAudioClip.getData(), 0, audioData, thisAudioClipLength, thatAudioClipLength);
			return AudioClip.from(audioData, getFormat());
		}

		public AudioClip secondHalf() {
			long size = size();
			int halfSize = asInt(size / 2L);
			int length = asInt(size - halfSize);
			byte[] audioData = dataCopy(halfSize, length);
			return AudioClip.from(audioData, getFormat());
		}

		public long size() {
			return audio().size();
		}
	}

	/**
	 * {@link FunctionalInterface} defining a contract to clip {@link Audio} by size or time.
	 */
	@FunctionalInterface
	public interface AudioBufferFactory {

		int DEFAULT_COMPRESSION_RATIO = 1; // measured as ?:1, for example 10:1 (10 to 1); 1:1 is no compression
		int HUMAN_HEARING_HIGH_FREQUENCY = 20_000; // 20,000 Hz (20 kHz)
		int HUMAN_HEARING_LOW_FREQUENCY = 20; // 20 Hz
		int NYQUIST_FREQUENCY = 22_050; // 22.05 kHz

		byte[] newAudioBuffer();

	}

	@Getter(AccessLevel.PROTECTED)
	public static abstract class AbstractAudioBufferFactory implements AudioBufferFactory {

		public static AbstractAudioBufferFactory.Builder from(Audio audio) {
			return new Builder(audio);
		}

		private final Audio audio;

		private final AudioFormat audioFormat;

		private final AudioProperties audioProperties;

		protected AbstractAudioBufferFactory(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
			this.audio = AudioUtils.assertAudio(audio);
			this.audioFormat = AudioUtils.assertAudioFormat(audioFormat);
			this.audioProperties = AudioProperties.nullSafe(audioProperties);
		}

		// fraction of a second is possible
		protected double getAudioClipDurationInSeconds() {
			Duration audioClipDuration = getAudioProperties().getClipDuration();
			long audioClipDurationInMilliseconds = audioClipDuration.toMillis();
			return (double) audioClipDurationInMilliseconds / TimeUtils.MILLISECONDS_PER_SECOND;
		}

		protected AudioChannels getAudioChannels() {
			return AudioChannels.from(getAudioFormat());
		}

		protected Duration getAudioDuration() {
			return getAudio().getDuration();
		}

		protected int getAudioSizeInBits() {
			return getAudioSizeInBytes() * BITS_PER_BYTE;
		}

		protected int getAudioSizeInBytes() {
			return getAudio().getData().length;
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

			// Compute bit rate as number of samples per second multiplied by the size of a sample (frame) in bits
			if (isSampleMetadataAvailable()) {
				int sampleRate = getSampleRate();
				int sampleSize = getFrameSizeInBits();
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

		protected boolean isSampleMetadataAvailable() {
			return AudioUtils.isSpecified(getSampleRate()) && AudioUtils.isSpecified(getSampleSizeInBits());
		}

		// Frame Size * Bits / Byte
		// Sample Size in Bits * Number of Audio Channels
		protected int getFrameSizeInBits() {
			int frameSize = getFrameSizeInBytes();
			return AudioUtils.isSpecified(frameSize) ? frameSize * BITS_PER_BYTE
				: getSampleSizeInBits() * getAudioChannels().value();
		}

		protected int getFrameSizeInBytes() {
			return getAudioFormat().getFrameSize();
		}

		// Number of Samples / Second (measured in Hertz (Hz) or Kilohertz (kHz))
		protected int getSampleRate() {
			float sampleRate = getAudioFormat().getSampleRate();
			return asInt(sampleRate);
		}

		// Number of Bits / Sample
		protected int getSampleSizeInBits() {
			return getAudioFormat().getSampleSizeInBits();
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
		public byte[] newAudioBuffer() {
			int bytesPerSecond = bytesPerSecond();
			double seconds = getAudioClipDurationInSeconds();
			int bufferSize = asInt(Math.round(bytesPerSecond * seconds));
			return new byte[bufferSize];
		}

		@Getter(AccessLevel.PROTECTED)
		public static class Builder {

			private static final String MPEG = "mpeg";

			private final Audio audio;

			private AudioFormat audioFormat;

			private AudioProperties audioProperties;

			protected Builder(Audio audio) {
				this.audio = AudioUtils.assertAudio(audio);
			}

			public Builder in(AudioFormat audioFormat) {
				this.audioFormat = AudioUtils.assertAudioFormat(audioFormat);
				return this;
			}

			public Builder using(AudioProperties audioProperties) {
				this.audioProperties = AudioProperties.nullSafe(audioProperties);
				return this;
			}

			private boolean isMpegAudioFormat() {
				return describe(getAudioFormat()).contains(MPEG);
			}

			private String describe(AudioFormat audioFormat) {
				return getAudioFormat().getEncoding().toString().toLowerCase();
			}

			public AbstractAudioBufferFactory build() {
				return isMpegAudioFormat()
					? new MpegAudioBufferFactory(getAudio(), getAudioFormat(), getAudioProperties())
					: new WavAudioBufferFactory(getAudio(), getAudioFormat(), getAudioProperties());
			}
		}
	}

	// MP3
	static class MpegAudioBufferFactory extends AbstractAudioBufferFactory implements MpegMetadata {

		MpegAudioBufferFactory(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
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
	static class WavAudioBufferFactory extends AbstractAudioBufferFactory implements CompactDiscMetadata {

		// 44,100 samples per second * 16 bits per sample * 2 channels (stereo) uncompressed
		// 1,411,200 bits per second, 1411.2 kbps
		static final int CD_BIT_RATE = CD_SAMPLE_RATE * CD_SAMPLE_SIZE_IN_BITS * AudioChannels.STEREO.value();

		WavAudioBufferFactory(Audio audio, AudioFormat audioFormat, AudioProperties audioProperties) {
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

	// Compact Disc (CD)
	protected interface CompactDiscMetadata {
		int CD_SAMPLE_RATE = 44_100; // 44,100 Hz (44.1 kHz); 44,100 samples per second
		int CD_SAMPLE_SIZE_IN_BITS = 16; // Bit Depth | Bit Resolution
	}

	// MP3
	@SuppressWarnings("unused")
	protected interface MpegMetadata {
		int MP3_BIT_RATE_128 = 128_000; // 128 kbps (128,000 bits per second)
		int MP3_BIT_RATE_160 = 160_000; // 160 kbps; higher compression
		int MP3_BIT_RATE_192 = 192_000; // 192 kbps
		int MP3_BIT_RATE_320 = 320_000; // 320 kbps; low compression
		int MP3_BIT_RATE = MP3_BIT_RATE_160;
		int MP3_SAMPLE_RATE = 22_050;
		int MP3_SAMPLE_SIZE_IN_BITS = Math.round(asFloat(MP3_BIT_RATE) / asFloat(MP3_SAMPLE_RATE));
	}
}
