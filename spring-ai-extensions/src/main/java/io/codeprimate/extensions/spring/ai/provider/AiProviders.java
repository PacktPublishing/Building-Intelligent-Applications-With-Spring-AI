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
package io.codeprimate.extensions.spring.ai.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.codeprimate.extensions.util.Utils;

import org.cp.elements.util.stream.Streamable;

/**
 * {@link Iterable Collection} of {@link AiProvider AiProviders}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.lang.Iterable
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see org.cp.elements.util.stream.Streamable
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AiProviders extends Iterable<AiProvider>, Streamable<AiProvider> {

	static AiProviders empty() {
		return Collections::emptyIterator;
	}

	static AiProviders of(AiProvider... aiProviders) {
		return of(Arrays.asList(aiProviders));
	}

	static AiProviders of(Iterable<AiProvider> aiProviders) {
		return Utils.nullSafeIterable(aiProviders)::iterator;
	}

	default Optional<AiProvider> findBy(Predicate<AiProvider> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default Optional<AiProvider> findByName(String aiProviderName) {
		return findBy(aiProvider -> aiProvider.getName().equalsIgnoreCase(aiProviderName));
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	@Override
	default Stream<AiProvider> stream() {
		return Utils.stream(this);
	}
}
