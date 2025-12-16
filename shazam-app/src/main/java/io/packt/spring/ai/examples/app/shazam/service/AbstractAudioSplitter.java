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

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asFloat;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.UuidGenerator;

import org.slf4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class encapsulating functionality common to all {@link AudioSplitter} implementations.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSplitter
 * @see AudioProperties
 * @see org.springframework.ai.document.Document
 * @see org.springframework.beans.factory.InitializingBean
 * @since 0.1.0
 */
@Slf4j
public abstract class AbstractAudioSplitter implements AudioSplitter, InitializingBean {

	private final AudioProperties audioProperties;

	public AbstractAudioSplitter(AudioProperties audioProperties) {
		Assert.notNull(audioProperties, "AudioProperties are required");
		this.audioProperties = audioProperties;
	}

	protected AudioProperties getAudioProperties() {
		return this.audioProperties;
	}

	protected Logger getLogger() {
		return log;
	}

	@Override
	public void afterPropertiesSet() {
		getLogger().info("Using AudioSplitter [{}]", this);
	}

	protected Document buildDocument(AudioClip audioClip) {

		return Document.builder()
			.idGenerator(UuidGenerator.INSTANCE)
			.text(audioClip.encode())
			.build();
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
	 */
	@SuppressWarnings("unused")
	public record AudioClip(Audio audio) {

		public AudioClip {
			Assert.notNull(audio, "Audio is required");
		}

		public static AudioClip from(byte[] audioData) {
			return new AudioClip(Audio.from(audioData));
		}

		public byte[] data() {
			return audio().getData();
		}

		private byte[] dataCopy(int position, int length) {
			byte[] dataCopy = new byte[length];
			System.arraycopy(data(), position, dataCopy, 0, length);
			return dataCopy;
		}

		public String encode() {
			return audio().encode();
		}

		public AudioClip firstHalf() {
			int length = Long.valueOf(size() / 2).intValue();
			byte[] audioData = dataCopy(0, length);
			return AudioClip.from(audioData);
		}

		public AudioClip merge(AudioClip audioClip) {
			int audioLength = Long.valueOf(size()).intValue();
			int audioClipLength = Long.valueOf(audioClip.size()).intValue();
			int length = audioLength + audioClipLength;
			byte[] audioData = new byte[length];
			System.arraycopy(data(), 0, audioData, 0, audioLength);
			System.arraycopy(audioClip.data(), 0, audioData, audioLength, audioClipLength);
			return AudioClip.from(audioData);
		}

		public AudioClip secondHalf() {
			long size = size();
			int halfSize = Long.valueOf(size / 2).intValue();
			int length = Long.valueOf(size - halfSize).intValue();
			byte[] audioData = dataCopy(halfSize, length);
			return AudioClip.from(audioData);
		}

		public long size() {
			return audio().size();
		}
	}

	// CD
	protected interface CompactDiscMetadata {

		int CD_SAMPLE_RATE = 44_100; // 44,100 Hz (44.1 kHz); 44,100 samples per second
		int CD_SAMPLE_SIZE_IN_BITS = 16; // AKA Bit Depth | Bit Resolution

	}

	// MP3
	@SuppressWarnings("unused")
	protected interface MpegLayer3Metadata {

		int MP3_BIT_RATE_STANDARD_QUALITY = 128_000; // 128 kbps (128,000 bits per second)
		int MP3_BIT_RATE_HIGH_QUALITY = 320_000; // 320 kbps; low compression
		int MP3_BIT_RATE_LOW_QUALITY = 64_000; // 64 kbps; high compression
		int MP3_BIT_RATE_AVG_QUALITY = (MP3_BIT_RATE_STANDARD_QUALITY + MP3_BIT_RATE_LOW_QUALITY) / 2;
		int MP3_BIT_RATE = MP3_BIT_RATE_STANDARD_QUALITY;
		int MP3_SAMPLE_RATE = 22_050;
		int MP3_SAMPLE_SIZE_IN_BITS = Math.round(asFloat(MP3_BIT_RATE) / asFloat(MP3_SAMPLE_RATE));

	}
}
