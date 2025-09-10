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
package com.packt.spring.ai.examples.tokenization;

import java.util.Scanner;

import com.knuddels.jtokkit.api.EncodingType;

import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * {@link SpringBootApplication} using Spring AI and the {@link TokenCountEstimator} to estimate the {@literal tokens}
 * used by a {@literal prompt} in OpenAI ChatGPT models, such as {@literal gpt-4o}.
 *
 * @author John Blum
 * @see org.springframework.ai.tokenizer.JTokkitTokenCountEstimator
 * @see org.springframework.ai.tokenizer.TokenCountEstimator
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class TokenizerApplication {

	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(TokenizerApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	TokenCountEstimator tokenCountEstimator(@Value("${examples.tokenizer.encoding-type}") String encodingTypeName) {

		EncodingType encodingType = EncodingType.fromName(encodingTypeName).orElseThrow(() -> {
			String message = "EncodingType [%s] not found".formatted(encodingTypeName);
			return new IllegalArgumentException(message);
		});

		return new JTokkitTokenCountEstimator(encodingType);
	}

	@Bean
	ApplicationRunner programRunner(TokenCountEstimator tokenCountEstimator) {

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String input;

			printPrompt();

			while (isNotExit(input = scanner.nextLine())) {
				if (StringUtils.hasText(input)) {
					int tokenCount = tokenCountEstimator.estimate(input);
					print("%d%n", tokenCount);
				}
				printPrompt();
			}
		};
	}

	private boolean isNotExit(String value) {
		return !(StringUtils.hasText(value) && "exit".equalsIgnoreCase(value.trim()));
	}

	private void print(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}

	private void printPrompt() {
		print("prompt> ");
	}
}
