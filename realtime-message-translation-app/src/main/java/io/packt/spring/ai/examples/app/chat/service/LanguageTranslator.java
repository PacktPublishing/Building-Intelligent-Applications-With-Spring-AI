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
package io.packt.spring.ai.examples.app.chat.service;

import io.packt.spring.ai.examples.app.chat.model.ChatMessage;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;

/**
 * {@link FunctionalInterface} defining a contract to translate a {@link String message}
 * into the given {@link IsoLanguage language}.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @see IsoLanguage
 * @since 0.1.0
 */
@FunctionalInterface
public interface LanguageTranslator {

	default String translate(String text, IsoLanguage language) {
		return translate(text, IsoLanguage.fromDefault(), language);
	}

	default ChatMessage translate(ChatMessage message, IsoLanguage language) {

		String translatedText = translate(message.message(), message.language(), language);

		return ChatMessage.from(message)
			.with(translatedText)
			.in(language)
			.build();
	}

	@SuppressWarnings("unused")
	default TextMessage translate(TextMessage message, IsoLanguage language) {
		String translatedText = translate(message.getText(), message.getLanguage(), language);
		return TextMessage.from(translatedText, language);
	}

	String translate(String text, IsoLanguage inLanguage, IsoLanguage toLanguage);

}
