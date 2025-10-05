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

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.UuidIdGenerator;

import org.slf4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class encapsulating functionality common to all {@link AudioSplitter} implementations.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSplitter
 * @since 0.1.0
 */
@Slf4j
public abstract class AbstractAudioSplitter implements AudioSplitter {

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

	protected Document buildDocument(AudioClip audioClip) {

		return Document.builder()
			.idGenerator(UuidIdGenerator.INSTANCE)
			.text(audioClip.encode())
			.build();
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
			int length = size() / 2;
			byte[] audioData = dataCopy(0, length);
			return AudioClip.from(audioData);
		}

		public AudioClip merge(AudioClip audioClip) {
			byte[] audioData = new byte[size() + audioClip.size()];
			System.arraycopy(data(), 0, audioData, 0, size());
			System.arraycopy(audioClip.data(), 0, audioData, size(), audioClip.size());
			return AudioClip.from(audioData);
		}

		public AudioClip secondHalf() {
			int size = size();
			int halfSize = size / 2;
			int length = size - halfSize;
			byte[] audioData = dataCopy(halfSize, length);
			return AudioClip.from(audioData);
		}

		public int size() {
			return audio().size();
		}
	}
}
