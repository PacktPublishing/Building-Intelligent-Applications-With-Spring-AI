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
package io.packt.spring.ai.examples.app.chat;

import io.codeprimate.extensions.spring.boot.AbstractDesktopSpringBootApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama and Meta's Llama3.2 AI model
 * to demonstrate real-time chat translation in a chat application between multiple users.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractDesktopSpringBootApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(ChatApplication.CHAT_PROFILE)
@SuppressWarnings("unused")
public class ChatApplication extends AbstractDesktopSpringBootApplication {

	protected static final String CHAT_PROFILE = "chat";

	public static void main(String[] args) {
		runSpringServletWebApplication(ChatApplication.class, asStringArray(CHAT_PROFILE), args);
	}

	@Override
	protected String getWebApplicationUrl() {
		return super.getWebApplicationUrl().concat("/view/join");
	}
}
