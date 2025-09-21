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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.LanguageTranslator;
import io.packt.spring.ai.examples.app.chat.service.MonologueRemover;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} implementing {@link LanguageTranslator} to translate {@link String messages}
 * into the {@link IsoLanguage language} using AI.
 *
 * @author John Blum
 * @see LanguageTranslator
 * @see TextMessage
 * @see MonologueRemover
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
		"Translate the written text \"{text}\" in {inLanguage} to {toLanguage}."
			+ " Return only the translated text enclosed in square brackets: [ ... ]."
			+ " Do not provide an explanation or pronunciation."
			+ " If the text does not need to be translated then simply return the text.";

	private final ChatClient chatClient;

	private final Map<TranslationKey, String> translationsMap = Collections.synchronizedMap(new WeakHashMap<>());

	private final MonologueRemover monologueRemover;

	@Override
	@SuppressWarnings("all")
	public String translate(String text, IsoLanguage inLanguage, IsoLanguage toLanguage) {

		if (isTranslatable(text, inLanguage, toLanguage)) {
			if (inLanguage.isNotEqualTo(toLanguage)) {

				TranslationKey translationKey = TranslationKey.from(text, inLanguage);

				return getTranslationsMap().computeIfAbsent(translationKey, key -> {

					PromptTemplate promptTemplate = new PromptTemplate(MESSAGE_TRANSLATION_PROMPT_TEMPLATE);

					promptTemplate.add("text", text);
					promptTemplate.add("inLanguage", inLanguage);
					promptTemplate.add("toLanguage", toLanguage);

					// Translate ChatMessage using AI
					String translatedMessage = getChatClient().prompt()
						.messages(promptTemplate.createMessage())
						.call()
						.content();

					TextMessage noMonologueTranslatedMessage =
						getMonologueRemover().removeMonologue(TextMessage.from(translatedMessage));

					return noMonologueTranslatedMessage.getText();
				});
			}
		}

		return text;
	}

	private boolean isTranslatable(String text, IsoLanguage fromLanguage, IsoLanguage toLanguage) {

		return StringUtils.hasText(text)
			&& Objects.nonNull(fromLanguage)
			&& Objects.nonNull(toLanguage);
	}

	record TranslationKey(int hash, IsoLanguage language) {

		static TranslationKey from(String text, IsoLanguage language) {
			Assert.hasText(text, "Text is required");
			Assert.notNull(language, "Language is required");
			int textHash = Objects.hash(text);
			return new TranslationKey(textHash, language);
		}
	}
}
