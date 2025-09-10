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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling a {@link String message} sent by a {@link ChatUser user}
 * in {@link ChatSession chat}.
 *
 * @author John Blum
 * @see java.lang.Comparable
 * @see java.time.Instant
 * @see java.util.UUID
 * @see ChatUser
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record ChatMessage(UUID id, Instant timestamp, ChatUser user, IsoLanguage language, String message,
		ChatMessages translatedMessages) implements Comparable<ChatMessage> {

	private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String TO_STRING = "[%s] %s: \"%s\"";

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

	public ChatMessage {
		Assert.notNull(id, "ID is required");
		Assert.notNull(user, "User is required");
		Assert.notNull(language, "Language is required");
		Assert.notNull(timestamp, "Timestamp is required");
		Assert.hasText(message, () -> "Message [%s] is required".formatted(message));
	}

	public static ChatMessage from(ChatUser user, String message) {
		return from(user, null,  message);
	}

	public static ChatMessage from(ChatUser user, IsoLanguage language, String message) {
		Assert.notNull(user, "User is required");
		IsoLanguage resolvedLanguage = resolveLanguage(language, user);
		return new ChatMessage(UUID.randomUUID(), Instant.now(), user, resolvedLanguage, message,
			ChatMessages.empty().mutable());
	}

	private static IsoLanguage resolveLanguage(IsoLanguage language, ChatUser user) {
		return language != null ? language : user.language();
	}

	public boolean add(ChatMessage translation) {
		Assert.notNull(translation, "Translated ChatMessage is required");
		Assert.state(translatedMessages().findBy(translation.language()).isEmpty(),
			() -> "ChatMessage with translation [%s] already exists".formatted(translation.language()));
		return translatedMessages().add(translation);
	}

	public Optional<ChatMessage> findBy(IsoLanguage language) {
		return translatedMessages().findBy(language);
	}

	public String getFormattedId() {
		return id().toString();
	}

	public String getFormattedTimestamp() {
		return LocalDateTime.from(timestamp()).format(TIMESTAMP_FORMATTER);
	}

	public String getUsername() {
		return user().name();
	}

	@Override
	public int compareTo(ChatMessage that) {
		return this.timestamp().compareTo(that.timestamp());
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ChatMessage that)) {
			return false;
		}

		return this.id().equals(that.id());
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(id());
	}

	@Override
	public String toString() {
		return TO_STRING.formatted(getFormattedTimestamp(), getUsername(), message());
	}
}
