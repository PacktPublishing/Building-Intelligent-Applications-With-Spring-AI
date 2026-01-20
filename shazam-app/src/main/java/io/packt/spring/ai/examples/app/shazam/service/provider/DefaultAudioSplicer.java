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

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplicer;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the {@link AudioSplicer}
 *
 * @author John Blum
 * @see Audio
 * @see AudioSplicer
 * @see org.springframework.ai.document.Document
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
public class DefaultAudioSplicer implements AudioSplicer {

	@Override
	public Audio splice(List<Document> audioDocuments) {

		List<byte[]> audioClipData = audioDocuments.stream()
			.filter(this::isNonOverlappingAudioClip)
			.map(this::extractAudioData)
			.toList();

		byte[] audioData = audioClipData.stream()
			.reduce(this::splice)
			.orElse(NumberUtils.EMPTY_BYTE_ARRAY);

		return Audio.from(audioData);
	}

	private boolean isNonOverlappingAudioClip(Document document) {
		return Boolean.FALSE.equals(document.getMetadata().get(AbstractAudioSplitter.AUDIO_CLIP_OVERLAP_KEY));
	}

	private byte[] extractAudioData(Document document) {
		return document.getMedia().getDataAsByteArray();
	}

	private byte[] splice(byte[] audioClipOne, byte[] audioClipTwo) {

		byte[] audioData = new byte[audioClipOne.length + audioClipTwo.length];

		System.arraycopy(audioClipOne, 0, audioData, 0, audioClipOne.length);
		System.arraycopy(audioClipTwo, 0, audioData, audioClipOne.length, audioClipTwo.length);

		return audioData;
	}
}
