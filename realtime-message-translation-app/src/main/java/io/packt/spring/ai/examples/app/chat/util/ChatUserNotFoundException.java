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

import java.util.UUID;

import io.packt.spring.ai.examples.app.chat.model.ChatUser;

/**
 * Java {@link RuntimeException} throw when a {@link ChatUser} cannot be found.
 *
 * @author John Blum
 * @see RuntimeException
 * @see ChatUser
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ChatUserNotFoundException extends RuntimeException {

	public static ChatUserNotFoundException from(UUID userId) {
		String message = "User with ID [%s] not found".formatted(userId);
		return new ChatUserNotFoundException(message);
	}

	public static ChatUserNotFoundException from(String username) {
		String message = "User [%s] not found".formatted(username);
		return new ChatUserNotFoundException(message);
	}

	public ChatUserNotFoundException() {

	}

	public ChatUserNotFoundException(String message) {
		super(message);
	}

	public ChatUserNotFoundException(Throwable cause) {
		super(cause);
	}

	public ChatUserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
