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
package com.packt.spring.ai.examples.connect4;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

import com.packt.spring.ai.examples.connect4.model.Play;
import com.packt.spring.ai.examples.connect4.model.Player;

import io.codeprimate.extensions.spring.ai.converter.JsonSanitizerOutputConverter;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.env.Environment;

/**
 * Abstract base class encapsulating common components and functionality used to implements the Connect 4 application.
 *
 * @author John Blum
 * @see AbstractSpringBootApplication
 * @see Environment
 * @see ChatClient
 * @since 0.1.0
 */
public abstract class AbstractConnectFourApplication extends AbstractSpringBootApplication {

	static final SecureRandom SECURE_RANDOM = new SecureRandom(UUID.randomUUID().toString().getBytes());

	@SuppressWarnings("unused")
	Play promptMockModel(String model, Map<String, Object> promptTemplateArguments, ChatClient chatClient) {

		String availableColumns = String.valueOf(promptTemplateArguments.get("availableColumns"));
		String letters = StringUtils.getLetters(availableColumns);

		int index = SECURE_RANDOM.nextInt(letters.length());

		String letter = String.valueOf(letters.charAt(index));

		return Play.from(letter, "Because");
	}

	Play promptRealModel(String model, Map<String, Object> promptTemplateArguments, ChatClient chatClient) {

		BeanOutputConverter<Play> playConverter = new BeanOutputConverter<>(Play.class);
		JsonSanitizerOutputConverter<Play> jsonConverter = JsonSanitizerOutputConverter.from(playConverter);

		return chatClient.prompt()
			.system(systemPromptTemplate())
			.user(promptUserSpec -> promptUserSpec
				.text(userPromptTemplate(promptTemplateArguments))
				.params(promptTemplateArguments))
			.options(Utils.buildChatOptions(model))
			.call()
			.entity(jsonConverter);
	}

	String resolveModel(Environment environment, Player player) {

		SpringAiProvider aiProvider = (SpringAiProvider) player.provider();

		String propertyName = SpringAiProvider.SPRING_AI_CHAT_OPTIONS_MODEL_PROPERTY_TEMPLATE
			.formatted(aiProvider.getPropertyName());

		return environment.getProperty(propertyName);
	}

	abstract String systemPromptTemplate();

	abstract String userPromptTemplate(Map<String, Object> promptTemplateArguments);

}
