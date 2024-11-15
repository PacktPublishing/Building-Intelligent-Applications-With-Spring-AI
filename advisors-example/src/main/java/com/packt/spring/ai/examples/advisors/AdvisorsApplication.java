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
package com.packt.spring.ai.examples.advisors;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.config.EnableRateLimit;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.StringUtils;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to demonstrate the Advisors API.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.client.advisor.api.Advisor
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class AdvisorsApplication {

	protected static final String EMPTY = "";
	protected static final String EXIT = "exit";

	public static void main(String[] args) {

		new SpringApplicationBuilder(AdvisorsApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@SpringBootConfiguration
	@EnableChatClient
	@EnableRateLimit(count = 1, duration = "15s")
	static class AdvisorsConfiguration {

		@Bean
		DefaultConversionService conversionService() {
			return new DefaultConversionService();
		}
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient, DefaultConversionService conversionService) {

		return args -> {

			Scanner scanner = new Scanner(System.in);

			String promptTemplate = "What are the main characters in {input}?";
			String input;

			print("What are the main characters in: ");

			while (isNotExit(input = scanner.nextLine())) {

				Map<String, Object> promptArguments = Map.of("input", input);

				Consumer<ChatClient.PromptUserSpec> promptUserSpecConsumer = promptUserSpec ->
					promptUserSpec.text(promptTemplate).params(promptArguments);

				List<String> response = chatClient.prompt()
					.user(promptUserSpecConsumer)
					.call()
					.entity(new ListOutputConverter(conversionService));

				String output = response.stream()
					.map("* %s"::formatted)
					.reduce("%s%n%s"::formatted)
					.orElse(EMPTY);

				print(output);
				print("%n%nWhat are the main characters in: ");
			}
		};
	}

	private boolean isNotExit(String input) {
		return StringUtils.hasText(input) && !EXIT.equalsIgnoreCase(input.trim());
	}

	private void print(String text) {
		System.out.printf(text);
		System.out.flush();
	}
}
