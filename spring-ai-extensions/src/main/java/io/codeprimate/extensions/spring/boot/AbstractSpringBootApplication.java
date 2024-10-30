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
package io.codeprimate.extensions.spring.boot;

import java.util.Scanner;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for all {@link SpringBootApplication SpringBootApplications}.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationArguments
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@Slf4j
@SuppressWarnings("unused")
public abstract class AbstractSpringBootApplication {

	protected static final String AI_PROMPT = "ai> %s%n";
	protected static final String EMPTY_STRING = "";
	protected static final String EXIT = "exit";
	protected static final String USER_PROMPT = "user> %s";

	// REPL
	protected ApplicationRunner readEvaluatePrintLoop(BiConsumer<ApplicationArguments, String> consumer) {

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String input;

			userPrompt();

			while (isNotExit(input = scanner.nextLine())) {
				if (StringUtils.hasText(input)) {
					consumer.accept(args, input);
				}

				userPrompt();
			}
		};
	}

	// Alias
	protected ApplicationRunner repl(BiConsumer<ApplicationArguments, String> consumer) {
		return readEvaluatePrintLoop(consumer);
	}

	protected String getContent(ChatResponse chatResponse) {
		return chatResponse.getResult().getOutput().getContent();
	}

	protected Logger getLogger() {
		return log;
	}

	private boolean isExit(String value) {
		return EXIT.equalsIgnoreCase(StringUtils.trimAllWhitespace(value));
	}

	private boolean isNotExit(String value) {
		return !isExit(value);
	}

	protected String outputAiResponse(String generatedContent) {
		print(AI_PROMPT, generatedContent);
		return generatedContent;
	}

	protected void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	protected void userPrompt() {
		print(EMPTY_STRING);
	}

	protected void userPrompt(String userMessage) {
		print(USER_PROMPT, userMessage);
	}
}
