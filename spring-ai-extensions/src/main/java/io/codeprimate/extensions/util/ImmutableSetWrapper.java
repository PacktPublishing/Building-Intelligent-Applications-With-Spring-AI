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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Immutable {@link Set} implementation wrapping and decorating an existing {@link Set}.
 *
 * @author John Blum
 * @see java.util.AbstractSet
 * @see java.util.Set
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ImmutableSetWrapper<E> extends AbstractSet<E> {

	public static <E> ImmutableSetWrapper<E> from(Set<E> set) {
		return new ImmutableSetWrapper<>(set);
	}

	private final Set<E> set;

	ImmutableSetWrapper(Set<E> set) {
		Assert.notNull(set, "Set to wrap is required");
		this.set = set;
	}

	public void ifNotEmpty(Consumer<Set<E>> consumer) {
		if (isNotEmpty()) {
			consumer.accept(this);
		}
	}

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	@Override
	public @NonNull Iterator<E> iterator() {
		return getSet().iterator();
	}

	@Override
	public int size() {
		return getSet().size();
	}
}
