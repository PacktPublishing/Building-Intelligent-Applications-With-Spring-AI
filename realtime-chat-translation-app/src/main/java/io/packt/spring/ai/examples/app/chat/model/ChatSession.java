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

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.packt.spring.ai.examples.app.chat.util.ChatUserNotFoundException;
import io.packt.spring.ai.examples.app.chat.util.InvalidChatSessionException;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a chat session with users communicating in chat.
 *
 * @author John Blum
 * @see java.lang.Comparable
 * @see java.lang.Iterable
 * @see ChatUser
 * @since 0.1.0
 */
@ToString(of = "id")
@Getter(AccessLevel.PUBLIC)
@SuppressWarnings("unused")
public class ChatSession implements Comparable<ChatSession>, Iterable<ChatUser> {

	public static final Duration INACTIVE_TIMEOUT = Duration.ofMinutes(5);
	public static final Duration PRESENCE_TIMEOUT = Duration.ofMinutes(2);

	public static ChatSession withUser(ChatUser user) {
		ChatSession session = new ChatSession(UUID.randomUUID());
		session.add(user);
		return session;
	}

	private final UUID id;

	private final Instant timestamp;

	private final ChatMessages messages;

	private final ChatUsers users;

	public ChatSession(UUID id) {

		Assert.notNull(id, "Chat Session ID is required");

		this.id = id;
		this.timestamp = Instant.now();
		this.messages = ChatMessages.empty().mutable();
		this.users = ChatUsers.empty().mutable();
	}

	private void assertChatMessage(ChatMessage message) {

		Assert.notNull(message, "ChatMessage is required");

		ChatUser user = message.user();

		try {
			getUsers().findBy(user.id());
		}
		catch (ChatUserNotFoundException cause) {
			throw InvalidChatSessionException.from(this, user, cause);
		}
	}

	public boolean isActive() {
		return getUsers().isNotEmpty();
	}

	public boolean add(ChatUser user) {
		return user != null && getUsers().add(user);
	}

	public List<ChatUser> findAll(Predicate<ChatUser> predicate) {
		return stream().filter(predicate).toList();
	}

	public Optional<ChatUser> findBy(Predicate<ChatUser> predicate) {
		return stream().filter(predicate).findFirst();
	}

	public ChatUser requireBy(UUID id) {
		return findBy(user -> user.id().equals(id))
			.orElseThrow(() -> ChatUserNotFoundException.from(id));
	}

	public ChatMessage post(ChatUser user, String message) {
		return post(ChatMessage.from(user, message));
	}

	public ChatMessage post(ChatMessage message) {
		assertChatMessage(message);
		getMessages().add(message);
		return message;
	}

	public boolean remove(ChatUser user) {
		return user != null && getUsers().remove(user);
	}

	public ChatUsers users() {
		return ChatUsers.of(this.users);
	}

	public List<ChatUser> usersExcluding(ChatUser user) {
		return findAll(existingUser -> existingUser.notEquals(user));
	}

	@Override
	public int compareTo(ChatSession that) {
		return this.getTimestamp().compareTo(that.getTimestamp());
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ChatSession that)) {
			return false;
		}

		return this.getId().equals(that.getId());
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(getId());
	}

	@Override
	public @NonNull Iterator<ChatUser> iterator() {
		return users().iterator();
	}

	public Stream<ChatUser> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
