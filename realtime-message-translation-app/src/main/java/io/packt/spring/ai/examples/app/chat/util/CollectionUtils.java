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
package io.packt.spring.ai.examples.app.chat.util;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Abstract base utility class for processing {@link Collection} objects.
 *
 * @author John Blum
 * @see Collection
 * @since 0.1.0
 */
public abstract class CollectionUtils {

	public static <T> Optional<T> lastElement(List<T> list) {
		int index = toIndex(nullSafeSize(list));
		return index > -1 ? Optional.ofNullable(list.get(index)) : Optional.empty();
	}

	public static Predicate<Collection<?>> notEmpty() {
		return collection -> collection != null && !collection.isEmpty();
	}

	public static int nullSafeSize(Collection<?> collection) {
		return notEmpty().test(collection) ? collection.size() : 0;
	}

	public static int toIndex(int count) {
		return count - 1;
	}
}
