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

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link Iterable} of {@link IsoLanguage} objects.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see IsoLanguage
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface IsoLanguages extends Iterable<IsoLanguage> {

	static IsoLanguages all() {
		return of(Arrays.stream(Locale.getAvailableLocales())
			.map(IsoLanguage::from)
			.sorted()
			.toList());
	}

	static IsoLanguages of(IsoLanguage... isoLanguages) {
		return of(Arrays.asList(isoLanguages));
	}

	static IsoLanguages of(Iterable<IsoLanguage> isoLanguages) {
		return isoLanguages::iterator;
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default Optional<IsoLanguage> findBy(Predicate<IsoLanguage> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default IsoLanguage findBy(String languageCode) {
		return findBy(language -> language.code().equals(languageCode)).orElseThrow(() -> {
			String message = "ISO language for code [%s] not found".formatted(languageCode);
			return new IllegalStateException(message);
		});
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	default Stream<IsoLanguage> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
