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
package io.codeprimate.extensions.data.struct;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.cp.elements.util.stream.StreamUtils;
import org.cp.elements.util.stream.Streamable;

/**
 * Abstract Data Type (ADT) modeling a {@link Iterable}, {@link Streamable} collection of elements.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @see Iterable
 * @see Streamable
 * @since 0.1.0
 */
@FunctionalInterface
public interface Collectable<T> extends Iterable<T>, Streamable<T> {

	default Optional<T> findBy(Predicate<T> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default boolean isEmpty() {
		return size() == 0L;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default long size() {
		return stream().count();
	}

	@Override
	default Stream<T> stream() {
		return StreamUtils.stream(this);
	}
}
