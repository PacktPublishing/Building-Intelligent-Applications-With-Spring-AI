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
package com.packt.spring.ai.examples.chat.client;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import io.codeprimate.extensions.spring.ai.converter.Rfc1123DateTimeStructuredOutputConverter;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

/**
 * {@link SpringBootApplication} using Spring AI with OpenAI ChatGPT ({@literal gpt-4o} model) to demonstrate
 * the {@link ChatClient} API by returning the current time for any location in the world.
 * <p>
 *     If the prompt is simply, "What time is it in London?", the AI will respond with:

 *     "I'm unable to provide real-time information, including the current time in London. However, you can easily find
 *     the current time by searching "current time in London" on a search engine or by checking the clock on your device
 *     set to London time."

 *     Therefore, this requires the application to provide the AI with context, or a point of reference.
 * </p>
 *
 * @author John Blum
 * @see java.time.ZonedDateTime
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.converter.StructuredOutputConverter
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class CurrentTimeApplication {

	private static final String EXIT = "exit";
	private static final String PROMPT_TEMPLATE_PROFILE = "prompt-template";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(CurrentTimeApplication.class)
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
	@Profile("!"+PROMPT_TEMPLATE_PROFILE)
	ApplicationRunner programRunner(ChatClient chatClient) {

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String location;

			print("Enter Location: ");

			while (isNotExit(location = scanner.nextLine())) {

				ZonedDateTime now = ZonedDateTime.now();

				String prompt = "If the current time in {zone} is {dateTime},"
					+ " in {format}, what time is it in {location}?";

				String format = Rfc1123DateTimeStructuredOutputConverter.INSTANCE.getFormat();

				Map<String, Object> promptArguments = Map.of(
					"zone", now.getZone().getId(),
					"dateTime", now.format(DateTimeFormatter.RFC_1123_DATE_TIME),
					"format", format,
					"location", location
				);

				Consumer<ChatClient.PromptUserSpec> userPrompt = promptUserSpec ->
					promptUserSpec.text(prompt).params(promptArguments);

				ZonedDateTime locationDateTime = chatClient.prompt()
					.user(userPrompt)
					.call()
					.entity(Rfc1123DateTimeStructuredOutputConverter.INSTANCE);

				print("%s%n%n", locationDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
				print("Enter Location: ");
			}
		};
	}

	@Bean
	@Profile(PROMPT_TEMPLATE_PROFILE)
	ApplicationRunner programRunnerUsingPromptTemplate(ChatClient chatClient) {

		System.out.println("Using Prompt Template");

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String location;

			print("Enter Location: ");

			while (isNotExit(location = scanner.nextLine())) {

				ZonedDateTime now = ZonedDateTime.now();

				String template = "If the current time in {zone} is {dateTime},"
					+ " in {format}, what time is it in {location}?";

				String format = Rfc1123DateTimeStructuredOutputConverter.INSTANCE.getFormat();

				PromptTemplate promptTemplate =new PromptTemplate(template);

				promptTemplate.add("zone", now.getZone().getId());
				promptTemplate.add("dateTime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
				promptTemplate.add("format", format);
				promptTemplate.add("location", location);

				ZonedDateTime locationDateTime = chatClient.prompt(promptTemplate.create()).call()
					.entity(Rfc1123DateTimeStructuredOutputConverter.INSTANCE);

				print("> %s%n%n", locationDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
				print("Enter Location: ");
			}
		};
	}

	private boolean isNotExit(String input) {
		return StringUtils.hasText(input) && !EXIT.equalsIgnoreCase(input.trim());
	}

	private void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}
}
