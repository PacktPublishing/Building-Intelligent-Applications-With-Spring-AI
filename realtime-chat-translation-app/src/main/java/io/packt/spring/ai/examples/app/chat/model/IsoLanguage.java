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

import java.util.Locale;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling a ISO 2 letter language code and language name.
 *
 * @author John Blum
 * @param code {@link String} containing the ISO 2 letter language code (e.g. en).
 * @param name {@link String} containing a user-friendly language name (e.g. English).
 * @see java.lang.Comparable
 * @see java.util.Locale
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record IsoLanguage(String code, String name) implements Comparable<IsoLanguage> {

	public static final IsoLanguage CHINESE =
		IsoLanguage.from(Locale.CHINESE.getLanguage(), Locale.CHINESE.getDisplayLanguage());

	public static final IsoLanguage ENGLISH =
		IsoLanguage.from(Locale.ENGLISH.getLanguage(), Locale.ENGLISH.getDisplayLanguage());

	public static final IsoLanguage FRENCH =
		IsoLanguage.from(Locale.FRENCH.getLanguage(), Locale.FRENCH.getDisplayLanguage());

	public static final IsoLanguage GERMAN =
		IsoLanguage.from(Locale.GERMAN.getLanguage(), Locale.GERMAN.getDisplayLanguage());

	public static final IsoLanguage JAPANESE =
		IsoLanguage.from(Locale.JAPANESE.getLanguage(), Locale.JAPANESE.getDisplayLanguage());

	public static final IsoLanguage SPANISH = IsoLanguage.from("es", "Spanish");

	public IsoLanguage {
		Assert.hasText(code, () -> "ISO language code [%s] is required".formatted(code));
		Assert.hasText(name, () -> "ISO language name [%s] is required".formatted(name));
	}

	public static IsoLanguage from(Locale locale) {
		Assert.notNull(locale, "Locale is required");
		return from(locale.getLanguage(), locale.getDisplayLanguage());
	}

	public static IsoLanguage from(String languageCode, String languageName) {
		return new IsoLanguage(languageCode, languageName);
	}

	public static IsoLanguage fromDefault() {
		return from(Locale.getDefault());
	}

	@Override
	public int compareTo(IsoLanguage that) {
		return this.name().compareTo(that.name());
	}

	public boolean isNotEqualTo(IsoLanguage language) {
		return !equals(language);
	}

	@Override
	public String toString() {
		return name();
	}
}
