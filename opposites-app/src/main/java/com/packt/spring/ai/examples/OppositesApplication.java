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
package com.packt.spring.ai.examples;

import java.util.function.Consumer;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} usig Spring AI with Ollama ({@literal llama3.2} model) to get the opposite of
 * a user-provided value.
 * <p/>
 * For example, when the user prompts "day", AI should generate a response of "night".
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class OppositesApplication extends AbstractSpringBootApplication {

	private static final String EXIT = "exit";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(OppositesApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient) {

		print("The opposite of: ");

		return repl((args, input) -> {

			Consumer<ChatClient.PromptUserSpec> promptUserSpecConsumer = promptUserSpec ->
				promptUserSpec.text("In a single word, what is opposite of {value}?").param("value", input);

			String opposite = chatClient.prompt()
				.user(promptUserSpecConsumer)
				.call()
				.content();

			print("%s%n%n", opposite);
			print("The opposite of: ");
		});
	}
}
