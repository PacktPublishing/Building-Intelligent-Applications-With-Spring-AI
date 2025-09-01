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

import io.packt.spring.ai.examples.app.chat.service.TextToSpeechSynthesizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} implementing {@link TextToSpeechSynthesizer} to synthesis speech (audio signals)
 * from {@link String written text}.
 *
 * @author John Blum
 * @see TextToSpeechSynthesizer
 * @see Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AiTextToSpeechSynthesizer implements TextToSpeechSynthesizer {

	private final ChatClient chatClient;

	@Override
	public byte[] speak(String text) {
		throw new UnsupportedOperationException("Text-To-Speech not implemented");
	}
}
