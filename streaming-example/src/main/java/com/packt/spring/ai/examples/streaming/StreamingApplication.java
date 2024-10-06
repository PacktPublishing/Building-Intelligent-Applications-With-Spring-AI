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
package com.packt.spring.ai.examples.streaming;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal llama3.2} model) to demonstrate
 * the Streaming API.
 * <p>
 * PROMPTS:
 * Please explain Cosine Similarity Search and how it works.
 * Recite the poem 'Twinkle Twinkle Little Star'.
 * </p>
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.chat.prompt.Prompt
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class StreamingApplication {

	private static final String EXIT = "exit";

	public static void main(String[] args) {

		new SpringApplicationBuilder(StreamingApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient) {

		return args -> {

			AtomicBoolean actionPerformed = new AtomicBoolean(false);
			Scanner scanner = new Scanner(System.in);
			String input;

			System.out.printf("%nuser> ");

			while (isNotExit(input = scanner.nextLine())) {
				if (StringUtils.hasText(input)) {

					Prompt prompt = new Prompt(input);

					chatClient.prompt(prompt).stream().chatResponse()
						.map(ChatResponse::getResults)
						.flatMapIterable(list -> list)
						.map(Generation::getOutput)
						.map(AssistantMessage::getContent)
						.map(String::toCharArray)
						.flatMapIterable(this::toListOfCharacters)
						.delayElements(Duration.ofMillis(5))
						.doOnComplete(() -> {
							actionPerformed.set(false);
							System.out.printf("%n%nuser> ");
						})
						.doOnNext(character -> {
							if (actionPerformed.compareAndSet(false, true)) {
								System.out.printf("%nai> ");
							}
						})
						.subscribe(System.out::print);
				}
				else {
					System.out.print("user> ");
				}
			}
		};
	}

	private boolean isNotExit(String input) {
		return !(StringUtils.hasText(input) && EXIT.equalsIgnoreCase(input.trim()));
	}

	private void printHelloWorld() {

		String helloWorld = "Hello World!";

		Flux.range(0, helloWorld.length())
			.map(helloWorld::charAt)
			.zipWith(Flux.interval(Duration.ofMillis(100)), (character, index) -> character)
			.doOnComplete(System.out::println)
			.subscribe(System.out::print);
	}

	private List<Character> toListOfCharacters(char[] array) {

		List<Character> characters = new ArrayList<>(array.length);

		for (char character : array) {
			characters.add(character);
		}

		return characters;
	}
}
