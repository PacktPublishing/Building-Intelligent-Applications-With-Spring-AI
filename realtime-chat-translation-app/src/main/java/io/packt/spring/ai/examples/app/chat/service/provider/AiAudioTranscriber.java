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
package io.packt.spring.ai.examples.app.chat.service.provider;

import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.AudioTranscriber;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link AudioTranscriber} implementation using OpenAI.
 *
 * @author John Blum
 * @see AudioTranscriber
 * @see org.springframework.ai.openai.OpenAiAudioTranscriptionModel
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AiAudioTranscriber implements AudioTranscriber {

	private final OpenAiAudioTranscriptionModel model;

	@Override
	public TextMessage transcribe(AudioMessage audioMessage) {
		Resource audio = audioMessage.getResource();
		String text = getModel().call(audio);
		return TextMessage.from(text);
	}
}
