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

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

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
 * @see java.lang.Iterable
 * @see ChatUser
 * @since 0.1.0
 */
@ToString(of = "id")
@Getter(AccessLevel.PUBLIC)
@SuppressWarnings("unused")
public class ChatSession implements Comparable<ChatSession>, Iterable<ChatUser> {

	public static ChatSession withUser(ChatUser user) {
		ChatSession session = new ChatSession(UUID.randomUUID());
		session.add(user);
		return session;
	}

	private final UUID id;

	private final Instant timestamp;

	private final ChatMessages chatMessages;

	private final ChatUsers chatUsers;

	public ChatSession(UUID id) {

		Assert.notNull(id, "Chat Session ID is required");

		this.id = id;
		this.timestamp = Instant.now();
		this.chatMessages = ChatMessages.empty().mutable();
		this.chatUsers = ChatUsers.empty().mutable();
	}

	public boolean isActive() {
		return getChatUsers().isNotEmpty();
	}

	public boolean add(ChatUser user) {
		return getChatUsers().add(user);
	}

	public boolean post(ChatMessage chatMessage) {
		assertChatMessage(chatMessage);
		return getChatMessages().add(chatMessage);
	}

	private void assertChatMessage(ChatMessage chatMessage) {

		Assert.notNull(chatMessage, "ChatMessage is required");

		ChatUser user = chatMessage.user();

		try {
			getChatUsers().findBy(user.id());
		}
		catch (ChatUserNotFoundException cause) {
			throw InvalidChatSessionException.from(this, user, cause);
		}
	}

	public boolean remove(ChatUser user) {
		return getChatUsers().remove(user);
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
		return ChatUsers.of(this.chatUsers).iterator();
	}
}
