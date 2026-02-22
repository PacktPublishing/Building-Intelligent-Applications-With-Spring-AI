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

	protected byte[] extractAudioClip(Document document) {
		return document.getMedia().getDataAsByteArray();
	}

	protected List<byte[]> extractAudioClips(List<Document> audioDocuments) {

		return audioDocuments.stream()
			.filter(audioClipFilter())
			.map(this::extractAudioClip)
			.toList();
	}

	protected byte[] spliceAudioClips(byte[] audioClipOne, byte[] audioClipTwo) {

		byte[] audioData = new byte[audioClipOne.length + audioClipTwo.length];

		System.arraycopy(audioClipOne, 0, audioData, 0, audioClipOne.length);
		System.arraycopy(audioClipTwo, 0, audioData, audioClipOne.length, audioClipTwo.length);

		return audioData;
	}

	protected byte[] spliceAudioClips(List<byte[]> audioClips) {

		return audioClips.stream()
			.reduce(this::spliceAudioClips)
			.orElse(NumberUtils.EMPTY_BYTE_ARRAY);
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
