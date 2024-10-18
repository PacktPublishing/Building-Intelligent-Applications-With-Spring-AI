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
package io.codeprimate.extensions.spring.ai.config;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Java {@link RuntimeException} thrown when a specific {@link ChatClient} cannot be found.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 */
@SuppressWarnings("unused")
public class ChatModelNotFoundException extends RuntimeException {

	public ChatModelNotFoundException() { }

	public ChatModelNotFoundException(String message) {
		super(message);
	}

	public ChatModelNotFoundException(Throwable cause) {
		super(cause);
	}

	public ChatModelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
