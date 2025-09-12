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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.cp.elements.lang.Assert;

/**
 * Abstract Data Type (ADT) modeling a user in a {@link ChatSession chat}.
 *
 * @author John Blum
 * @param id {@link UUID} identifying the user;
 * @param name {@link String} containing the name of the user.
 * @param language {@link IsoLanguage} preferred language spoken and understood by the user.
 * @see java.time.Instant
 * @see java.util.UUID
 * @see IsoLanguage
 * @see Comparable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record ChatUser(UUID id, String name, IsoLanguage language, AtomicReference<Instant> presentTimestamp,
		AtomicReference<Instant> lastMessageReceivedTimestamp, Instant joinedTimestamp) implements Comparable<ChatUser> {

	public ChatUser {
		Assert.notNull(id, "ChatUser ID is required");
		Assert.hasText(name, "ChatUser name [%s] is required".formatted(name));
		Assert.notNull(language, "ChatUser language [%s] is required".formatted(language));
	}

	public static ChatUser from(String name) {
		return from(name, IsoLanguage.fromDefault());
	}

	public static ChatUser from(String name, IsoLanguage language) {
		return new ChatUser(generateId(), name, language,
			nowTimestampReference(), epochTimestampReference(), joinedTimestampInstant());
	}

	private static UUID generateId() {
		return UUID.randomUUID();
	}

	private static AtomicReference<Instant> epochTimestampReference() {
		return new AtomicReference<>(Instant.EPOCH);
	}

	private static Instant joinedTimestampInstant() {
		return Instant.now();
	}

	private static AtomicReference<Instant> nowTimestampReference() {
		return new AtomicReference<>(Instant.now());
	}

	public boolean isPresent(Duration timeout) {
		Assert.notNull(timeout, "Timeout is required to determine user presence");
		long presentMilliseconds = System.currentTimeMillis() - getPresentTimestamp().toEpochMilli();
		return Duration.ofMillis(presentMilliseconds).compareTo(timeout) > 0;
	}

	public Instant getLastReceivedTimestamp() {
		return lastMessageReceivedTimestamp().get();
	}

	public Instant getPresentTimestamp() {
		return presentTimestamp().get();
	}

	public void present() {
		presentTimestamp().set(Instant.now());
	}

	public void received(ChatMessage chatMessage) {

		Instant chatMessageTimestamp = chatMessage.timestamp();

		if (chatMessageTimestamp.isAfter(getLastReceivedTimestamp())) {
			lastMessageReceivedTimestamp().set(chatMessageTimestamp);
		}
	}

	@Override
	public int compareTo(ChatUser that) {
		return this.name().compareTo(that.name());
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ChatUser that)) {
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
		return name();
	}
}
