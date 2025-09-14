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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.packt.spring.ai.examples.app.chat.util.ChatMessageNotFoundException;
import io.packt.spring.ai.examples.app.chat.util.CollectionUtils;

import org.cp.elements.lang.Assert;
import org.springframework.lang.NonNull;

/**
 * {@link Iterable} of {@link ChatMessage} objects.
 *
 * @author John Blum
 * @see ChatMessage
 * @see Iterable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface ChatMessages extends Iterable<ChatMessage> {

	static ChatMessages empty() {
		return Collections::emptyIterator;
	}

	static ChatMessages of(ChatMessage... chatMessages) {
		return of(Arrays.asList(chatMessages));
	}

	static ChatMessages of(Iterable<ChatMessage> chatMessages) {
		return chatMessages::iterator;
	}

	default boolean add(ChatMessage chatMessage) {
		String message = "Cannot add messages to an immutable ChatMessages object; call mutable()";
		throw new UnsupportedOperationException(message);
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	// Finds chat messages after a given timestamp that the given user has not received.
	default ChatMessages findAfter(Instant timestamp, ChatUser user) {

		Predicate<ChatMessage> chatMessagesAfterTimestampNotSentByUser = chatMessage ->
			chatMessage.timestamp().isAfter(timestamp) && !chatMessage.user().equals(user);

		List<ChatMessage> chatMessageList = stream()
			.filter(chatMessagesAfterTimestampNotSentByUser)
			.sorted()
			.toList();

		return ChatMessages.of(chatMessageList);
	}

	default ChatMessages findAll(Predicate<ChatMessage> predicate) {
		return ChatMessages.of(stream().filter(predicate).toList());
	}

	default Optional<ChatMessage> findBy(Predicate<ChatMessage> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default ChatMessage findBy(UUID id) {
		return findBy(chatMessage -> chatMessage.id().equals(id))
			.orElseThrow(() -> ChatMessageNotFoundException.from(id));
	}

	default Optional<ChatMessage> findBy(IsoLanguage language) {
		return findBy(chatMessage -> chatMessage.language().equals(language));
	}

	default Optional<ChatMessage> findLast() {
		return CollectionUtils.lastElement(toList());
	}

	default ChatMessages mutable() {

		List<ChatMessage> chatMessages = Collections.synchronizedList(new ArrayList<>(toList()));

		return new ChatMessages() {

			private ChatMessage assertChatMessage(ChatMessage chatMessage) {
				Assert.notNull(chatMessage, "Chat message is required");
				Assert.state(chatMessages.stream().noneMatch(it -> it.equals(chatMessage)),
					() -> "ChatMessage with ID [%s] already exists".formatted(chatMessage.id()));
				return chatMessage;
			}

			@Override
			public boolean add(ChatMessage chatMessage) {
				assertChatMessage(chatMessage);
				return chatMessages.add(chatMessage);
			}

			@Override
			public @NonNull Iterator<ChatMessage> iterator() {
				return chatMessages.iterator();
			}
		};
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	default Stream<ChatMessage> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default List<ChatMessage> toList() {
		return stream().sorted().toList();
	}

	default Set<ChatMessage> toSet() {
		return stream().collect(Collectors.toSet());
	}
}
