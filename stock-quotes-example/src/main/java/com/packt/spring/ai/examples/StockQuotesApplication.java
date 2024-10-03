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

import java.util.Scanner;
import java.util.function.Consumer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

// TODO: Example for Spring AI Functions
/**
 * {@link SpringBootApplication} used to return a Stock Quote given a Stock Symbol (for examples: AAPL).
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class StockQuotesApplication {

	private static final String EXIT = "exit";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(StockQuotesApplication.class)
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

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String input;

			System.out.print("Enter Stock Symbol: ");

			while (isNotExit(input = scanner.nextLine())) {

				String stockSymbol = input;

				Consumer<ChatClient.PromptUserSpec> promptUserSpecConsumer = promptUserSpec ->
					promptUserSpec.text("What is the current stock price for {stock}?").param("stock", stockSymbol);

				String stockPrice = chatClient.prompt()
					.user(promptUserSpecConsumer)
					.call()
					.content();

				System.out.printf("[%s]: %s%n%n", stockSymbol, stockPrice);
				System.out.print("Enter Stock Symbol: ");
			}
		};
	}

	private boolean isNotExit(String input) {
		return StringUtils.hasText(input) && !EXIT.equalsIgnoreCase(input);
	}
}
