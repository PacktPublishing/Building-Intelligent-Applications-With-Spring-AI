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
package io.packt.spring.ai.examples.app.chat.web.controller;

import java.time.Instant;
import java.util.UUID;

import io.packt.spring.ai.examples.app.chat.model.ChatMessage;
import io.packt.spring.ai.examples.app.chat.model.ChatMessages;
import io.packt.spring.ai.examples.app.chat.model.ChatSession;
import io.packt.spring.ai.examples.app.chat.model.ChatUser;
import io.packt.spring.ai.examples.app.chat.model.ChatUsers;
import io.packt.spring.ai.examples.app.chat.service.ChatService;

import org.cp.elements.lang.Assert;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Web MVC {@link RestController} used to process API requests, send and receive {@link ChatMessages},
 * query for existing {@link ChatUsers} and execute other chat related tasks.
 *
 * @author John Blum
 * @see RestController
 * @see ChatMessages
 * @see ChatUsers
 * @since 0.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ChatApiController {

	private final ChatService chatService;

	protected Logger getLogger() {
		return log;
	}

	@GetMapping("/{sessionId}/messages")
	public Iterable<ChatMessage> getChatMessages(@PathVariable("sessionId") UUID sessionId) {

		ChatSession session = getChatService().findBy(sessionId);
		ChatMessages messages = session.getMessages();

		return messages.toList();
	}

	@GetMapping("/{sessionId}/users/{userId}/messages")
	public Iterable<ChatMessage> getChatMessages(@PathVariable("sessionId") UUID sessionId,
			@PathVariable("userId") UUID userId) {

		ChatSession session = getChatService().findBy(sessionId);
		ChatUser user = session.requireBy(userId);
		ChatMessages messages = unreceivedMessagesByUser(session, user);

		return messages.toList();
	}

	private ChatMessages unreceivedMessagesByUser(ChatSession session, ChatUser user) {

		Instant timestamp = user.getLastReceivedTimestamp();

		ChatMessages messages = session.getMessages().findAfter(timestamp, user);

		ChatMessages translatedMessages = ChatMessages.of(messages.stream()
			.map(message -> getChatService().translateChatMessage(message, user.language()))
			.toList());

		messages.findLast().ifPresent(user::received);

		return translatedMessages;
	}

	@PostMapping("/{sessionId}/messages")
	public PostChatMessageResponse postChatMessage(@PathVariable("sessionId") UUID sessionId,
			@RequestBody PostChatMessageRequest request) {

		UUID userId = request.getUserId();

		String message = request.getMessage();

		ChatSession session = getChatService().findBy(sessionId);
		ChatUser user = session.requireBy(userId);
		ChatMessage chatMessage = session.post(user, message);

		getLogger().info("Chat Message [%s]".formatted(chatMessage));

		return PostChatMessageResponse.from(chatMessage);
	}

	@GetMapping("/{sessionId}/users")
	public ChatUsers getChatUsers(@PathVariable("sessionId") UUID sessionId) {
		ChatSession session = getChatService().findBy(sessionId);
		return session.allUsers();
	}

	@Data
	@ToString
	public static class PostChatMessageRequest {

		private String message;
		private UUID userId;

	}

	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "from")
	public static class PostChatMessageResponse {

		static PostChatMessageResponse from(ChatMessage message) {
			Assert.notNull(message, "ChatMessage is required");
			return from(message.id().toString());
		}

		private final String id;

	}
}
