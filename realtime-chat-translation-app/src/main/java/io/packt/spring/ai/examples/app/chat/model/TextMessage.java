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
package io.packt.spring.ai.examples.app.chat.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Abstract Data Type (ADT) modeling a {@link String text message}.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
public interface TextMessage {

	String NO_TEXT = "";

	static TextMessage empty() {
		return () -> NO_TEXT;
	}

	@JsonCreator
	static TextMessage from(String text) {
		return () -> text;
	}

	static TextMessage from(String text, IsoLanguage language) {

		return new TextMessage() {

			@Override
			public String getText() {
				return text;
			}

			@Override
			public IsoLanguage getLanguage() {
				return language;
			}
		};
	}

	static TextMessage from(ChatMessage message) {
		return from(message.message(), message.language());
	}

	default IsoLanguage getLanguage() {
		return IsoLanguage.fromDefault();
	}

	String getText();

}
