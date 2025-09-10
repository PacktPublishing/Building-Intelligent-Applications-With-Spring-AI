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

import lombok.AccessLevel;
import lombok.Getter;

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

	private static final String TO_STRING = "[%s] %s: \"%s\"";
	private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

	public ChatMessage {
		Assert.notNull(id, "ID is required");
		Assert.notNull(timestamp, "Timestamp is required");
		Assert.notNull(user, "User is required");
		Assert.notNull(language, "Language is required");
		Assert.hasText(message, () -> "Message [%s] is required".formatted(message));
	}

	public static UserBuilder from(String message) {
		return ChatMessageBuilder.from(message);
	}

	public static ChatMessage from(ChatUser user, String message) {
		return ChatMessageBuilder.from(message).by(user).inUserLanguage().build();
	}

	public ChatMessage add(ChatMessage translation) {

		Assert.notNull(translation, "Translated message is required");

		Assert.state(this.notEquals(translation),
			() -> "Message for language [%s] already exists".formatted(this.language()));

		Assert.state(translatedMessages().findBy(translation.language()).isEmpty(),
			() -> "Message with translation [%s] already exists".formatted(translation.language()));

		translatedMessages().add(translation);

		return this;
	}

	public Optional<ChatMessage> findBy(IsoLanguage language) {
		return this.language().equals(language) ? Optional.of(this)
			: translatedMessages().findBy(language);
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

		return language().equals(that.language())
			&& message().equalsIgnoreCase(that.message());
	}

	public boolean notEquals(Object obj) {
		return !equals(obj);
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(id());
	}

	@Override
	public String toString() {
		return TO_STRING.formatted(getFormattedTimestamp(), getUsername(), message());
	}

	@Getter(AccessLevel.PROTECTED)
	static class ChatMessageBuilder implements LanguageBuilder, MessageBuilder, UserBuilder {

		static ChatMessageBuilder from(String message) {
			return new ChatMessageBuilder(message);
		}

		private ChatUser user;

		private final Instant timestamp = Instant.now();

		private IsoLanguage language;

		private final String message;

		private final UUID id = UUID.randomUUID();

		private ChatMessageBuilder(String message) {
			Assert.hasText(message, () -> "Message [%s] is required".formatted(message));
			this.message = message;
		}

		@Override
		public LanguageBuilder by(ChatUser user) {
			Assert.notNull(user, "User is required");
			return null;
		}

		@Override
		public MessageBuilder in(IsoLanguage language) {
			Assert.notNull(language, "Language is required");
			this.language = language;
			return this;
		}

		@Override
		public MessageBuilder inUserLanguage() {
			this.language = getUser().language();
			return this;
		}

		public ChatMessage build() {
			return new ChatMessage(getId(), getTimestamp(), getUser(), getLanguage(), getMessage(),
				ChatMessages.empty().mutable());
		}
	}

	public interface LanguageBuilder {
		MessageBuilder in(IsoLanguage language);
		MessageBuilder inUserLanguage();
	}

	public interface MessageBuilder {
		ChatMessage build();
	}

	public interface UserBuilder {
		LanguageBuilder by(ChatUser user);
	}
}
