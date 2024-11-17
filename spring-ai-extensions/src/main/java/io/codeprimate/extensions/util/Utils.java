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
package io.codeprimate.extensions.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;

/**
 * Abstract utility class containing common, useful functions.
 *
 * @author John Blum
 */
@SuppressWarnings("unused")
public abstract class Utils {

	public static final String EMPTY_STRING = "";
	public static final String SINGLE_SPACE = " ";

	public static <T> T defaultIfNull(T value, T defaultValue) {
		return defaultIfNull(value, () -> defaultValue);
	}

	public static <T> T defaultIfNull(T value, Supplier<T> defaultValue) {
		return value != null ? value : defaultValue.get();
	}

	public static String generatedContent(Generation generation) {

		return Optional.ofNullable(generation)
			.map(Generation::getOutput)
			.map(AssistantMessage::getContent)
			.orElse(EMPTY_STRING);
	}

	public static boolean isNotEmpty(float[] array) {
		return array != null && array.length > 0;
	}

	public static <T> Iterator<T> nullSafeIterator(Iterable<T> iterable) {
		return nullSafeIterator(nullSafeIterable(iterable).iterator());
	}

	public static <T> Iterator<T> nullSafeIterator(Iterator<T> iterator) {
		return iterator != null ? iterator : Collections.emptyIterator();
	}

	public static <T> Consumer<T> nullSafeConsumer(Consumer<T> consumer) {
		return consumer != null ? consumer : target -> {};
	}

	public static <T> Iterable<T> nullSafeIterable(Iterable<T> iterable) {
		return iterable != null ? iterable : Collections::emptyIterator;
	}

	public static <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	public static <T> Set<T> nullSafeList(Set<T> set) {
		return set != null ? set : Collections.emptySet();
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return StreamSupport.stream(nullSafeIterable(iterable).spliterator(), false);
	}
}
