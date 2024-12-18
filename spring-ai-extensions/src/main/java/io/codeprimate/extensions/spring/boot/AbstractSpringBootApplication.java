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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for all {@link SpringBootApplication SpringBootApplications}.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationArguments
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 */
@SuppressWarnings("unused")
public abstract class AbstractSpringBootApplication {

	private static volatile ConfigurableApplicationContext applicationContext;

	protected static final Function<SpringApplicationBuilder, SpringApplicationBuilder> NON_WEB_APPLICATION_FUNCTION =
		springApplicationBuilder -> springApplicationBuilder.web(WebApplicationType.NONE);

	protected static final Function<SpringApplicationBuilder, SpringApplicationBuilder> REACTIVE_WEB_APPLICATION_FUNCTION =
		springApplicationBuilder -> springApplicationBuilder.web(WebApplicationType.SERVLET);

	protected static final Function<SpringApplicationBuilder, SpringApplicationBuilder> SERVLET_WEB_APPLICATION_FUNCTION =
		springApplicationBuilder -> springApplicationBuilder.web(WebApplicationType.SERVLET);

	protected static final String AI_PROMPT = "ai> %s%n";
	protected static final String EMPTY_STRING = "";
	protected static final String EXIT = "exit";
	protected static final String NEWLINE = System.lineSeparator();
	protected static final String USER_PROFILE = "user";
	protected static final String USER_PROMPT = "user> %s";

	protected static final String[] NO_PROFILES = new String[0];

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected static ConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected static SpringApplication runSpringApplication(Class<?> mainApplicationClass, String... args) {
		return runSpringApplication(mainApplicationClass, NO_PROFILES, Function.identity(), args);
	}

	protected static SpringApplication runSpringApplication(Class<?> mainApplicationClass, String[] profiles,
			String... args) {

		return runSpringApplication(mainApplicationClass, profiles, Function.identity(), args);
	}

	protected static SpringApplication runSpringApplication(Class<?> mainApplicationClass,
			Function<SpringApplicationBuilder, SpringApplicationBuilder> function, String... args) {

		return runSpringApplication(mainApplicationClass, NO_PROFILES, function, args);
	}

	@SuppressWarnings("all")
	protected static SpringApplication runSpringApplication(Class<?> mainApplicationClass, String[] profiles,
			Function<SpringApplicationBuilder, SpringApplicationBuilder> function, String... args) {

		assertApplicationContextClosed(applicationContext);

		SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(mainApplicationClass)
			.web(WebApplicationType.NONE)
			.profiles(resolveProfiles(profiles));

		springApplicationBuilder = function.apply(springApplicationBuilder);

		SpringApplication springApplication = springApplicationBuilder.build();

		applicationContext = springApplication.run(args);

		return springApplication;
	}

	private static void assertApplicationContextClosed(ConfigurableApplicationContext applicationContext) {

		Assert.state(isApplicationContextClosed(applicationContext),
			() -> "ApplicationContext [%s] already exists and is running".formatted(applicationContext.getId()));
	}

	private static boolean isApplicationContextClosed(ConfigurableApplicationContext applicationContext) {
		return applicationContext == null || applicationContext.isClosed();
	}

	protected static ConfigurableApplicationContext requireApplicationContext() {
		return ObjectUtils.requireState(applicationContext, "ApplicationContext not initialized");
	}

	private static String[] resolveProfiles(String... profiles) {

		List<String> profileList = Arrays.stream(ArrayUtils.nullSafeArray(profiles))
			.filter(StringUtils::hasText)
			.toList();

		if (!profileList.contains(USER_PROFILE)) {
			profileList = new ArrayList<>(profileList);
			profileList.add(USER_PROFILE);
		}

		return profileList.toArray(new String[0]);
	}

	protected static String[] useProfiles(String... profiles) {
		return profiles;
	}

	protected static void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	protected static void printNewline(int count) {

		IntStream.range(0, Math.max(0, count))
			.mapToObj(index -> NEWLINE)
			.reduce("%s%s"::formatted)
			.ifPresent(AbstractSpringBootApplication::print);
	}

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
		return Utils.generatedContent(chatResponse);
	}

	protected Logger getLogger() {
		return this.logger;
	}

	private boolean isExit(String value) {
		return EXIT.equalsIgnoreCase(StringUtils.trimAllWhitespace(value));
	}

	private boolean isNotExit(String value) {
		return !isExit(value);
	}

	private void logAtLevel(Predicate<Logger> levelPredicate, Consumer<Logger> logConsumer) {

		Logger logger = getLogger();

		if (levelPredicate.test(logger)) {
			logConsumer.accept(logger);
		}
	}

	protected void logDebug(String message, Object... arguments) {
		logAtLevel(Logger::isDebugEnabled, logger -> logger.debug(message, arguments));
	}

	protected void logTrace(String message, Object... arguments) {
		logAtLevel(Logger::isTraceEnabled, logger -> logger.trace(message, arguments));
	}

	protected void logInfo(String message, Object... arguments) {
		logAtLevel(Logger::isInfoEnabled, logger -> logger.info(message, arguments));
	}

	protected void logWarn(String message, Object... arguments) {
		logAtLevel(Logger::isWarnEnabled, logger -> logger.warn(message, arguments));
	}

	protected void logError(String message, Object... arguments) {
		logAtLevel(Logger::isErrorEnabled, logger -> logger.error(message, arguments));
	}

	protected String outputAiResponse(ChatResponse chatResponse) {
		return outputAiResponse(getContent(chatResponse));
	}

	protected String outputAiResponse(String generatedContent) {
		print(AI_PROMPT, generatedContent);
		return generatedContent;
	}

	protected void userPrompt() {
		userPrompt(EMPTY_STRING);
	}

	protected Message userPrompt(Message message) {
		userPrompt(message.getContent());
		return message;
	}

	protected Prompt userPrompt(Prompt prompt) {
		userPrompt(prompt.getContents());
		return prompt;
	}

	protected String userPrompt(String message) {
		print(USER_PROMPT, message);
		return message;
	}
}
