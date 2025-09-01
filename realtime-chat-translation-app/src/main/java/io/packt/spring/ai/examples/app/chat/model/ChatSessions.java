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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.packt.spring.ai.examples.app.chat.util.ChatSessionNotFoundException;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * {@link Iterable} of {@link ChatSession} objects.
 *
 * @author John Blum
 * @see ChatSession
 * @see Iterable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface ChatSessions extends Iterable<ChatSession> {

	static ChatSessions empty() {
		return Collections::emptyIterator;
	}

	static ChatSessions of(ChatSession... chatSessions) {
		return of(Arrays.asList(chatSessions));
	}

	static ChatSessions of(Iterable<ChatSession> chatSessions) {
		return chatSessions::iterator;
	}

	default boolean add(ChatSession session) {
		String message = "Cannot add new chat sessions to an immutable ChatSessions object; call mutable()";
		throw new UnsupportedOperationException(message);
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default Optional<ChatSession> findBy(Predicate<ChatSession> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default ChatSession findBy(UUID id) {
		return findBy(chatSession -> chatSession.getId().equals(id))
			.orElseThrow(() -> ChatSessionNotFoundException.from(id));
	}

	default ChatSessions mutable() {

		Set<ChatSession> chatSessions = new ConcurrentSkipListSet<>(toSet());

		return new ChatSessions() {

			private ChatSession assertChatSession(ChatSession chatSession) {
				Assert.notNull(chatSession, "ChatSession is required");
				Assert.state(chatSessions.stream().noneMatch(it -> it.getId().equals(chatSession.getId())),
					() -> "ChatSession with ID [%s] already exists".formatted(chatSession.getId()));
				return chatSession;
			}

			@Override
			public boolean add(ChatSession session) {
				assertChatSession(session);
				return chatSessions.add(session);
			}

			@Override
			public @NonNull Iterator<ChatSession> iterator() {
				return chatSessions.iterator();
			}

			@Override
			public boolean remove(ChatSession session) {
				return session != null && chatSessions.remove(session);
			}
		};
	}
	default boolean remove(ChatSession session) {
		String message = "Cannot remove chat sessions from an immutable ChatSessions object; call mutable()";
		throw new UnsupportedOperationException(message);
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	default Stream<ChatSession> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default Set<ChatSession> toSet() {
		return stream().collect(Collectors.toSet());
	}
}
