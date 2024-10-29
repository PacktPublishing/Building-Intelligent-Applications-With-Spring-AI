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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for all {@link SpringBootApplication SpringBootApplications}.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationArguments
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SuppressWarnings("unused")
public abstract class AbstractSpringBootApplication {

	protected static final String EXIT = "exit";

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

	private boolean isExit(String value) {
		return EXIT.equalsIgnoreCase(StringUtils.trimAllWhitespace(value));
	}

	private boolean isNotExit(String value) {
		return !isExit(value);
	}

	private void aiGeneratedOutput(String output) {
		print("ai> %s%n", output);
	}

	private void userPrompt() {
		print("user> ");
	}

	protected void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}
}
