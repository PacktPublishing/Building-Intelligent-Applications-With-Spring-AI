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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) and Java Record modeling a {@link String message} sent by a {@link ChatUser user}
 * in a {@link ChatSession chat}.
 *
 * @author John Blum
 * @see java.lang.Comparable
 * @see java.time.Instant
 * @see java.util.UUID
 * @see TextMessage
 * @see ChatUser
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record ChatMessage(UUID id, Instant timestamp, ChatUser user, IsoLanguage language, String message,
		ChatMessages translatedMessages) implements Comparable<ChatMessage> {

	private static final String CHAT_MESSAGE_TO_STRING = "[%s] %s: \"%s\"";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private static final String HOUR_MINUTE_PATTERN = "HH:mm";
	private static final String TIME_PATTERN = "HH:mm:ss";
	private static final String TIMESTAMP_PATTERN = DATE_PATTERN.concat(" ").concat(TIME_PATTERN);

	private static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(HOUR_MINUTE_PATTERN);
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

	public static MessageBuilder from(ChatMessage message) {
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

	public String getFormattedHourMinute() {
		return getZonedDatetime().format(HOUR_MINUTE_FORMATTER);
	}

	public String getFormattedTimestamp() {
		return getZonedDatetime().format(TIMESTAMP_FORMATTER);
	}

	public TextMessage getText() {
		return TextMessage.from(this);
	}

	public Set<ChatMessage> getTranslatedMessages() {
		return translatedMessages().toSet();
	}

	@JsonIgnore
	public ZonedDateTime getZonedDatetime() {
		return ZonedDateTime.ofInstant(timestamp(), ZoneId.systemDefault());
	}

	public String getUsername() {
		return user().name();
	}

	public boolean isInLanguage(IsoLanguage language) {
		return language().equals(language);
	}

	public boolean isNotInLanguage(IsoLanguage language) {
		return !isInLanguage(language);
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

	public boolean notEquals(Object obj) {
		return !equals(obj);
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(id());
	}

	@Override
	public String toString() {
		return CHAT_MESSAGE_TO_STRING.formatted(getFormattedTimestamp(), getUsername(), message());
	}

	@Getter(AccessLevel.PROTECTED)
	static class ChatMessageBuilder implements Builder<ChatMessage>, LanguageBuilder, UserBuilder {

		static ChatMessageBuilder from(String message) {
			return new ChatMessageBuilder(message);
		}

		static MessageBuilder from(ChatMessage chatMessage) {
			Assert.notNull(chatMessage, "ChatMessage to copy is required");
			return message -> new ChatMessageBuilder(chatMessage.timestamp(), message).by(chatMessage.user());
		}

		private ChatUser user;

		private final Instant timestamp;

		private IsoLanguage language;

		private final String message;

		private final UUID id;

		private ChatMessageBuilder(String message) {
			this(Instant.now(), message);
		}

		private ChatMessageBuilder(Instant timestamp, String message) {
			Assert.notNull(timestamp, "Timestamp is required");
			Assert.hasText(message, () -> "Message [%s] is required".formatted(message));
			this.timestamp = timestamp;
			this.message = message;
			this.id = generateId();
		}

		private UUID generateId() {
			return UUID.randomUUID();
		}

		@Override
		public LanguageBuilder by(ChatUser user) {
			Assert.notNull(user, "User is required");
			this.user = user;
			return this;
		}

		@Override
		public Builder<ChatMessage> in(IsoLanguage language) {
			Assert.notNull(language, "Language is required");
			this.language = language;
			return this;
		}

		@Override
		public Builder<ChatMessage> inUserLanguage() {
			this.language = getUser().language();
			return this;
		}

		public ChatMessage build() {
			return new ChatMessage(getId(), getTimestamp(), getUser(), getLanguage(), getMessage(),
				ChatMessages.empty().mutable());
		}
	}

	@FunctionalInterface
	public interface Builder<T> {
		T build();
	}

	public interface LanguageBuilder {
		Builder<ChatMessage> in(IsoLanguage language);
		Builder<ChatMessage> inUserLanguage();
	}

	@FunctionalInterface
	public interface MessageBuilder {
		LanguageBuilder with(String message);
	}

	@FunctionalInterface
	public interface UserBuilder {
		LanguageBuilder by(ChatUser user);
	}
}
