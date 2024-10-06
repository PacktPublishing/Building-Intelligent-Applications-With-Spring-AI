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
package com.packt.spring.ai.exmaples.prompts;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal llama3.2} model) to demonstrate
 * {@link Prompt Prompts} and {@link Message Messages}.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.messages.AssistantMessage
 * @see org.springframework.ai.chat.messages.Message
 * @see org.springframework.ai.chat.messages.UserMessage
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.chat.model.Generation
 * @see org.springframework.ai.chat.prompt.Prompt
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class PromptApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(PromptApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(ChatModel chatModel) {

		return args -> {

			Prompt prompt = print(new Prompt("Knock Knock"));

			Generation response = print(chatModel.call(prompt).getResult());

			AssistantMessage assistantMessage = response.getOutput();

			List<Message> messages = List.of(assistantMessage, new UserMessage("Orange"));

			prompt = print(new Prompt(messages));
			response = print(chatModel.call(prompt).getResult());

			// Close the deal!
			assistantMessage = response.getOutput();
			messages = List.of(assistantMessage, new UserMessage("Orange you glad you met me?"));
			prompt = print(new Prompt(messages));
			print(chatModel.call(prompt).getResult());
		};
	}

	private Prompt print(Prompt prompt) {

		print("user> %s%n%n", prompt.getInstructions().stream()
			.filter(UserMessage.class::isInstance)
			.map(Message::getContent)
			.reduce("%s %s"::formatted)
			.orElse(""));

		return prompt;
	}

	private Generation print(Generation generation) {
		print("ai> %s%n%n", generation.getOutput().getContent());
		return generation;
	}

	private void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}
}
