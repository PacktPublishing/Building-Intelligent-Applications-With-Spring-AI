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
package io.packt.spring.ai.examples.app.chat.util;

import io.packt.spring.ai.examples.app.chat.model.ChatSession;
import io.packt.spring.ai.examples.app.chat.model.ChatUser;

/**
 * Java {@link RuntimeException} thrown when the given {@link ChatSession} is not valid.
 *
 * @author John Blum
 * @see RuntimeException
 * @see ChatSession
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class InvalidChatSessionException extends RuntimeException {

	public static InvalidChatSessionException from(ChatSession session) {
		String message = "ChatSession with ID [%s] is not valid";
		return new InvalidChatSessionException(message);
	}

	public static InvalidChatSessionException from(ChatSession session, ChatUser user) {
		String message = "User [%s] is not a member of ChatSession [%s]".formatted(user.name(), session.getId());
		return new InvalidChatSessionException(message);
	}

	public InvalidChatSessionException() {

	}

	public InvalidChatSessionException(String message) {
		super(message);
	}

	public InvalidChatSessionException(Throwable cause) {
		super(cause);
	}

	public InvalidChatSessionException(String message, Throwable cause) {
		super(message, cause);
	}
}
