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

import java.util.List;
import java.util.function.Predicate;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.util.DocumentUtils;
import io.packt.spring.ai.examples.app.shazam.util.NumberUtils;

import org.springframework.ai.document.Document;

/**
 * Abstract base class encapsulating functionality common to all {@link AudioSplicer} implementations.
 *
 * @author John Blum
 * @see AudioSplicer
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
public abstract class AbstractAudioSplicer implements AudioSplicer {

	protected Predicate<Document> audioClipFilter() {
		return document -> true;
	}

	protected Audio extractAudioClip(Document document) {
		return DocumentUtils.toAudio(document);
	}

	protected List<Audio> extractAudioClips(List<Document> audioDocuments) {

		return audioDocuments.stream()
			.filter(audioClipFilter())
			.map(this::extractAudioClip)
			.toList();
	}

	protected byte[] extractAudioClipData(Document document) {
		return extractAudioClip(document).getData();
	}

	protected List<byte[]> extractAudioClipsData(List<Document> audioDocuments) {

		return audioDocuments.stream()
			.filter(audioClipFilter())
			.map(this::extractAudioClipData)
			.toList();
	}

	protected byte[] spliceAudioClipsData(byte[] audioClipDataOne, byte[] audioClipDataTwo) {

		byte[] audioData = new byte[audioClipDataOne.length + audioClipDataTwo.length];

		System.arraycopy(audioClipDataOne, 0, audioData, 0, audioClipDataOne.length);
		System.arraycopy(audioClipDataTwo, 0, audioData, audioClipDataOne.length, audioClipDataTwo.length);

		return audioData;
	}

	protected byte[] spliceAudioClipsData(List<byte[]> audioClipsData) {

		return audioClipsData.stream()
			.reduce(this::spliceAudioClipsData)
			.orElse(NumberUtils.EMPTY_BYTE_ARRAY);
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
