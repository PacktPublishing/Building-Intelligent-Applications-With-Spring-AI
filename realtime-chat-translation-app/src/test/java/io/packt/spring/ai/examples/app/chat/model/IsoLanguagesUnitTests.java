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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link IsoLanguages}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see IsoLanguages
 * @since 0.1.0
 */
public class IsoLanguagesUnitTests {

	private void assertLanguage(IsoLanguage language, String code, String name) {
		assertThat(language).isNotNull();
		assertThat(language.code()).isEqualTo(code);
		assertThat(language.name()).isEqualTo(name);
	}

	@Test
	void allIsoLanguages() {

		IsoLanguages languages = IsoLanguages.all();

		assertThat(languages).isNotNull();
		assertThat(languages.findBy("zh")).isEqualTo(IsoLanguage.from("zh", "Chinese"));
		assertThat(languages.findBy("en")).isEqualTo(IsoLanguage.from("en", "English"));
		assertThat(languages.findBy("fr")).isEqualTo(IsoLanguage.from("fr", "French"));
		assertThat(languages.findBy("de")).isEqualTo(IsoLanguage.from("de", "German"));
		assertThat(languages.findBy("it")).isEqualTo(IsoLanguage.from("it", "Italian"));
		assertThat(languages.findBy("ja")).isEqualTo(IsoLanguage.from("ja", "Japanese"));
		assertThat(languages.findBy("ko")).isEqualTo(IsoLanguage.from("ko", "Korean"));
		assertThat(languages.findBy("es")).isEqualTo(IsoLanguage.from("es", "Spanish"));
	}

	@Test
	void defaultLanguages() {

		assertLanguage(IsoLanguage.CHINESE, "zh", "Chinese");
		assertLanguage(IsoLanguage.ENGLISH, "en", "English");
		assertLanguage(IsoLanguage.FRENCH, "fr", "French");
		assertLanguage(IsoLanguage.GERMAN, "de", "German");
		assertLanguage(IsoLanguage.JAPANESE, "ja", "Japanese");
		assertLanguage(IsoLanguage.SPANISH, "es", "Spanish");
	}
}
