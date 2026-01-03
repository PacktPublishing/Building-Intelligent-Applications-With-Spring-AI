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
package io.codeprimate.extensions.data.caching;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.cp.elements.lang.Assert;
import org.springframework.lang.NonNull;

/**
 * Simple, read-only, {@literal Cache} data structure.
 *
 * @author John Blum
 * @see Cache.Entry
 * @see java.lang.Iterable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface Cache<KEY, VALUE> extends Iterable<Cache.Entry<KEY, VALUE>> {

	static <KEY, VALUE> Cache<KEY, VALUE> inMemory() {

		Map<KEY, VALUE> cache = new ConcurrentHashMap<>();

		return new Cache<>() {

			@Override
			public boolean evictAll() {
				cache.clear();
				return true;
			}

			@Override
			public VALUE evict(KEY key) {
				return key != null ? cache.remove(key) : null;
			}

			@Override
			public VALUE get(KEY key) {
				return key != null ? cache.get(key) : null;
			}

			@Override
			public @NonNull Iterator<Entry<KEY, VALUE>> iterator() {
				return cache.entrySet().stream()
					.map(Entry::from)
					.collect(Collectors.toSet())
					.iterator();
			}

			@Override
			public long size() {
				return cache.size();
			}

			@Override
			public void put(KEY key, VALUE value) {
				cache.put(key, value);
			}
		};
	}

	default boolean evictAll() {
		return false;
	}

	default VALUE evict(KEY key) {
		return null;
	}

	VALUE get(KEY key);

	default void put(KEY key, VALUE value) {
		throw new IllegalStateException("Cache is read-only");
	}

	default long size() {
		return stream().count();
	}

	default Stream<Entry<KEY, VALUE>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	interface Entry<KEY, VALUE> {

		static <KEY, VALUE> Entry<KEY, VALUE> from(KEY key, Cache<KEY, VALUE> cache) {

			Assert.notNull(key, "Key is required");
			Assert.notNull(cache, "Cache is required");

			return new Entry<>() {

				@Override
				public KEY getKey() {
					return key;
				}

				@Override
				public VALUE getValue() {
					return cache.get(key);
				}
			};
		}

		static <KEY, VALUE> Entry<KEY, VALUE> from(Map.Entry<KEY, VALUE> mapEntry) {

			Assert.notNull(mapEntry, "Map.Entry is required");

			return new Entry<>() {

				@Override
				public KEY getKey() {
					return mapEntry.getKey();
				}

				@Override
				public VALUE getValue() {
					return mapEntry.getValue();
				}
			};
		}

		KEY getKey();

		VALUE getValue();

	}
}
