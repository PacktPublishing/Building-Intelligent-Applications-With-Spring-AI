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
package com.packt.spring.ai.examples.metadata;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} using Spring AI with OpenAI and ChatGPT ({@literal gpt-4o} model) to generate content
 * and then look at the metadata returned by the AI provider.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.metadata.ChatResponseMetadata
 * @see org.springframework.ai.chat.metadata.RateLimit
 * @see org.springframework.ai.chat.metadata.Usage
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.chat.model.ChatResponse
 * @see org.springframework.ai.chat.prompt.Prompt
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class MetadataApplication {

	private static final String SINGLE_SPACE = " ";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(MetadataApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(ChatModel chatModel) {

		return args -> {

			//Prompt prompt = new Prompt("Summarize the difference between the theory of general relativity"
			//	+ " and quantum mechanics in 2 paragraphs.");

			Prompt prompt = new Prompt("Explain the theory of general relativity in a few paragraphs.");

			print("user> %s%n", getContent(prompt));

			ChatResponse response = chatModel.call(prompt);

			print("ai> %s%n%n", getContent(response));

			ChatResponseMetadata metadata = response.getMetadata();

			print("Prompt Word Count: %d%n", countWords(getContent(prompt)));
			print("Generation Word Count: %d%n", countWords(singleSpace(getContent(response))));

			Usage usage = metadata.getUsage();

			print("Prompt Tokens: %d%n", usage.getPromptTokens());
			print("Generation Tokens: %d%n", usage.getGenerationTokens());
			print("Total Tokens: %d%n", usage.getTotalTokens());

			RateLimit rateLimit = metadata.getRateLimit();

			print("Requests Limit: %d%n", rateLimit.getRequestsLimit());
			print("Requests Remaining: %d%n", rateLimit.getRequestsRemaining());
			print("Requests Reset (ms): %d%n", rateLimit.getRequestsReset().toMillis());
			print("Tokens Limit: %d%n", rateLimit.getTokensLimit());
			print("Tokens Remaining: %d%n", rateLimit.getTokensRemaining());
			print("Tokens Reset (ms): %d%n", rateLimit.getTokensReset().toMillis());
		};
	}

	private String getContent(Prompt prompt) {
		return prompt.getContents();
	}

	private String getContent(ChatResponse response) {
		return response.getResult().getOutput().getContent();
	}

	private int countWords(String value) {
		return value.split(SINGLE_SPACE).length;
	}

	private void print(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}

	private String singleSpace(String value) {
		return value.replaceAll("\\s+", " ");
	}
}
