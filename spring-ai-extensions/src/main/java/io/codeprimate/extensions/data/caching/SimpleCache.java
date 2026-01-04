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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.cp.elements.lang.Assert;
import org.springframework.lang.NonNull;

/**
 * Simple, read-only, {@literal SimpleCache cache} data structure.
 *
 * @author John Blum
 * @param <KEY> {@link Class type} of key.
 * @param <VALUE> {@link Class type} of value.
 * @see FunctionalInterface
 * @see java.lang.Iterable
 * @see SimpleCache.Entry
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface SimpleCache<KEY, VALUE> extends Iterable<SimpleCache.Entry<KEY, VALUE>> {

	/**
	 * Factory method used to construct a new {@link SimpleCache} from the given {@link Map}.
	 *
	 * @param <KEY> {@link Class type} of key.
	 * @param <VALUE> {@link Class type} of value.
	 * @param map {@link Map} to convert to a {@link SimpleCache}.
	 * @return a new {@link SimpleCache} from the given {@link Map}
	 * @throws IllegalArgumentException if {@link Map} is {@literal null}.
	 * @see Map
	 */
	static <KEY, VALUE> SimpleCache<KEY, VALUE> from(Map<KEY, VALUE> map) {

		Assert.notNull(map, "Map is required");

		Map<KEY, VALUE> cache = new ConcurrentHashMap<>(map);

		return new SimpleCache<>() {

			@Override
			public boolean evictAll() {
				throw new IllegalStateException("Cache is read-only");
			}

			@Override
			public VALUE evict(KEY key) {
				throw new IllegalStateException("Cache is read-only");
			}

			@Override
			public VALUE get(KEY key) {
				return cache.get(key);
			}

			@Override
			public VALUE get(KEY key, Function<KEY, VALUE> cacheLoader) {
				VALUE value = get(key);
				return value != null ? value : cacheLoader.apply(key);
			}

			@Override
			public @NonNull Iterator<Entry<KEY, VALUE>> iterator() {
				return cache.entrySet().stream()
					.map(SimpleCache.Entry::from)
					.collect(Collectors.toSet())
					.iterator();
			}

			@Override
			public long size() {
				return cache.size();
			}
		};
	}

	/**
	 * Factory method used to construct a new {@link SimpleCache} storing entries in memory.
	 *
	 * @param <KEY> {@link Class type} of cache key.
	 * @param <VALUE> {@link Class type} of value stored in cache.
	 * @return a new {@link SimpleCache}.
	 */
	static <KEY, VALUE> SimpleCache<KEY, VALUE> inMemory() {

		Map<KEY, VALUE> cache = new ConcurrentHashMap<>();

		return new SimpleCache<>() {

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
			public void put(KEY key, VALUE value) {
				cache.put(key, value);
			}

			@Override
			public long size() {
				return cache.size();
			}
		};
	}

	/**
	 * Removes all {@link SimpleCache.Entry entries} from this cache.
	 *
	 * @return a boolean value indicating whether the operation was successful in removing all entries from the cache.
	 * @see #evict(Object)
	 */
	default boolean evictAll() {
		return false;
	}

	/**
	 * Remove a single {@link SimpleCache.Entry} identified by the given {@link KEY key} from this cache.
	 *
	 * @return the value currently mapped to the given {@link KEY key}; may be {@literal null}.
	 * @see #evictAll()
	 */
	default VALUE evict(KEY key) {
		return null;
	}

	/**
	 * Gets the {@link VALUE value} of the {@link SimpleCache.Entry} identified by the given {@link KEY key}.
	 * <p>
	 * Returns {@literal null} on cache miss.
	 *
	 * @param key {@link KEY key} identifying the {@link SimpleCache.Entry} who's value is returned.
	 * @return the {@link VALUE value} of the {@link SimpleCache.Entry} identified by the given {@link KEY key}.
	 * Returns {@literal null} on cache miss.
	 * @see #get(Object, Function)
	 * @see #put(Object, Object)
	 */
	default VALUE get(KEY key) {
		return null;
	}

	/**
	 * Gets the {@link VALUE value} of the {@link SimpleCache.Entry} identified by the given {@link KEY key}.
	 * <p>
	 * Invokes the given {@link Function cache loader} on cache miss to compute the {@link VALUE value}.
	 *
	 * @param key {@link KEY key} identifying the {@link SimpleCache.Entry} who's value is returned.
	 * @return the {@link VALUE value} of the {@link SimpleCache.Entry} identified by the given {@link KEY key}.
	 * Invokes the given {@link Function cache loader} on cache miss to compute the {@link VALUE value}.
	 * @see #put(Object, Object)
	 * @see #get(Object)
	 * @see Function
	 */
	default VALUE get(KEY key, Function<KEY, VALUE> cacheLoader) {

		VALUE value = get(key);

		if (value == null) {
			value = cacheLoader.apply(key);
			put(key, value);
		}

		return value;
	}

	/**
	 * Stores the given {@link VALUE value} in this cache mapped by the given {@link KEY key}.
	 * <p>
	 * Throws {@link IllegalStateException} by default given the cache is read-only by default.
	 *
	 * @param key {@link KEY key} identifying the {@link VALUE value} stored in this cache.
	 * @param value {@link VALUE value} to store in this cache mapped by the given {@link KEY key}.
	 * @see #get(Object)
	 */
	default void put(KEY key, VALUE value) {
		throw new IllegalStateException("Cache is read-only");
	}

	/**
	 * Returns the number of {@link SimpleCache.Entry entries} in this cache.
	 *
	 * @return the number of {@link SimpleCache.Entry entries} in this cache.
	 */
	default long size() {
		return stream().count();
	}

	/**
	 * Returns a {@link Stream} of {@link SimpleCache.Entry entries} stored in this cache.
	 *
	 * @return a {@link Stream} of {@link SimpleCache.Entry entries} stored in this cache.
	 * @see Stream
	 */
	default Stream<Entry<KEY, VALUE>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Concerts this {@link SimpleCache} to a {@link Map}.
	 *
	 * @return a {@link Map} from this {@link SimpleCache}.
	 * @see Map
	 */
	default Map<KEY, VALUE> toMap() {
		return stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	/**
	 * Abstract Data Type (ADT) modeling an entry in a cache.
	 *
	 * @param <KEY> {@link Class type} of key.
	 * @param <VALUE> {@link Class type} of value.
	 */
	interface Entry<KEY, VALUE> {

		/**
		 * Factory method used to construct a new {@link SimpleCache.Entry} representing the {@link VALUE value}
		 * stored in the given {@link SimpleCache} identified with the given {@link KEY key}.
		 *
		 * @param <KEY> {@link Class type} of key.
		 * @param <VALUE> {@link Class type} of value.
		 * @param key {@link KEY key} identifying the {@link VALUE value} stored in the given {@link SimpleCache}
		 * returned by the {@link SimpleCache.Entry}.
		 * @param cache {@link SimpleCache} storing the {@link VALUE value} mapped with the given {@link KEY key}.
		 * @return the {@link VALUE value} stored in the given {@link SimpleCache}
		 * identified with the given {@link KEY key}.
		 * @throws IllegalArgumentException if {@link KEY} or {@link SimpleCache cache} are {@literal null}.
		 * @see SimpleCache
		 */
		static <KEY, VALUE> Entry<KEY, VALUE> from(KEY key, SimpleCache<KEY, VALUE> cache) {

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

		/**
		 * Factory method used to construct a new {@link SimpleCache.Entry} from a given {@link Map.Entry}.
		 *
		 * @param <KEY> {@link Class type} of key.
		 * @param <VALUE> {@link Class type} of value.
		 * @param mapEntry {@link Map.Entry} to convert into a {@link SimpleCache.Entry}.
		 * @return a new {@link SimpleCache.Entry} from a given {@link Map.Entry}.
		 * @throws IllegalArgumentException if {@link Map.Entry} is {@literal null}.
		 * @see Map.Entry
		 */
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

		/**
		 * {@link KEY key} of this {@link SimpleCache.Entry}.
		 *
		 * @return the {@link KEY key} of this {@link SimpleCache.Entry}.
		 */
		KEY getKey();

		/**
		 * {@link VALUE value} of this {@link SimpleCache.Entry}.
		 *
		 * @return the {@link VALUE value} of this {@link SimpleCache.Entry}.
		 */
		VALUE getValue();

	}
}
