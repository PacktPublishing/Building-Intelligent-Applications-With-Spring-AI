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

import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.service.LanguageTranslator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} implementing {@link LanguageTranslator} to translate {@link String messages}
 * into the {@link IsoLanguage language} using AI.
 *
 * @author John Blum
 * @see LanguageTranslator
 * @see IsoLanguage
 * @see ChatClient
 * @see Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AiLanguageTranslator implements LanguageTranslator {

	protected static final String MESSAGE_TRANSLATION_PROMPT_TEMPLATE =
		"Translate the written text \"{text}\" to {language}. Return only the translation. Do not provide any explanation or pronunciation.";

	private final ChatClient chatClient;

	// TODO: Use synchronization so a given chat message is only translated once
	@Override
	@SuppressWarnings("all")
	public String translate(String message, IsoLanguage language) {

		PromptTemplate promptTemplate = new PromptTemplate(MESSAGE_TRANSLATION_PROMPT_TEMPLATE);

		promptTemplate.add("text", message);
		promptTemplate.add("language", language);

		// Translate ChatMessage using AI
		String translatedMessage = getChatClient().prompt()
			.messages(promptTemplate.createMessage())
			.call()
			.content();

		return translatedMessage;
	}
}
