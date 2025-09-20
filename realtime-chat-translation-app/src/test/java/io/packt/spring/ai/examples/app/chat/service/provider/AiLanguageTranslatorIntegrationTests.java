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

import static org.assertj.core.api.Assertions.assertThat;

import io.packt.spring.ai.examples.app.chat.config.ChatConfiguration;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.service.LanguageTranslator;
import io.packt.spring.ai.examples.app.chat.service.MonologueRemover;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests for {@link AiLanguageTranslator}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see AiLanguageTranslator
 * @since 0.1.0
 */
@SpringBootTest
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class AiLanguageTranslatorIntegrationTests {

	@Autowired
	private LanguageTranslator languageTranslator;

	@Test
	void translateWords() {
		assertThat(getLanguageTranslator().translate("Hello", IsoLanguage.SPANISH)).containsIgnoringCase("Hola");
		assertThat(getLanguageTranslator().translate("Hello", IsoLanguage.GERMAN)).containsIgnoringCase("Hallo");
		assertThat(getLanguageTranslator().translate("Hello", IsoLanguage.FRENCH)).containsIgnoringCase("Bonjour");
	}

	@Test
	void translateSentence() {

		assertThat(getLanguageTranslator().translate("My name is John", IsoLanguage.SPANISH).toLowerCase().trim())
			.matches("mi nombre es (john|juan)\\.?");

		assertThat(getLanguageTranslator().translate("My name is John", IsoLanguage.GERMAN).toLowerCase().trim())
			.matches("mein name ist john\\.?");

		assertThat(getLanguageTranslator().translate("My name is John", IsoLanguage.FRENCH).toLowerCase().trim())
			.matches("mon nom est (john|jean)\\.?"); // Je m'appelle Jean
	}

	@Test
	void translationUnnecessary() {
		assertThat(getLanguageTranslator().translate("Where is my pen?", IsoLanguage.ENGLISH))
			.isEqualToIgnoringCase("Where is my pen?");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ChatConfiguration.class)
	static class TestConfiguration {

		@Bean
		LanguageTranslator languageTranslator(ChatClient chatClient) {
			return new AiLanguageTranslator(chatClient, MonologueRemover.noop());
		}
	}
}
