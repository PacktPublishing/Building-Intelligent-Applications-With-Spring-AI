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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.packt.spring.ai.examples.app.chat.util.ChatUserNotFoundException;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * {@link Iterable} of {@link ChatUser} objects.
 *
 * @author John Blum
 * @see ChatUser
 * @see Iterable
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ChatUsers extends Iterable<ChatUser> {

	static ChatUsers empty() {
		return Collections::emptyIterator;
	}

	static ChatUsers of(ChatUser... chatUsers) {
		return of(Arrays.asList(chatUsers));
	}

	static ChatUsers of(Iterable<ChatUser> chatUsers) {
		return chatUsers::iterator;
	}

	default boolean add(ChatUser user) {
		String message = "Cannot add users to an immutable ChatUsers object; call mutable()";
		throw new UnsupportedOperationException(message);
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default Optional<ChatUser> findBy(Predicate<ChatUser> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default Optional<ChatUser> findBy(UUID userId) {
		return findBy(user -> user.id().equals(userId));
	}

	default Optional<ChatUser> findBy(String username) {
		return findBy(user -> user.name().equals(username));
	}

	default ChatUsers mutable() {

		List<ChatUser> users = new CopyOnWriteArrayList<>(toList());

		return new ChatUsers() {

			private ChatUser assertChatUser(ChatUser user) {
				Assert.notNull(user, "User is required");
				Assert.notNull(user.id(), "User ID is required");
				Assert.state(users.stream().noneMatch(it -> it.id().equals(user.id())),
					() -> "User with ID [%s] already exists".formatted(user.id()));
				return user;
			}

			@Override
			public boolean add(ChatUser user) {
				assertChatUser(user);
				return users.add(user);
			}

			@Override
			public @NonNull Iterator<ChatUser> iterator() {
				return users.iterator();
			}

			@Override
			public boolean remove(ChatUser user) {
				return user != null && users.remove(user);
			}
		};
	}

	default boolean remove(ChatUser user) {
		String message = "Cannot remove users from an immutable ChatUsers object; call mutable()";
		throw new UnsupportedOperationException(message);
	}

	default ChatUser requireBy(UUID userId) {
		return findBy(userId).orElseThrow(() -> ChatUserNotFoundException.from(userId));
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	default Stream<ChatUser> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default List<ChatUser> toList() {
		return stream().toList();
	}

	default Set<ChatUser> toSet() {
		return stream().collect(Collectors.toSet());
	}
}
