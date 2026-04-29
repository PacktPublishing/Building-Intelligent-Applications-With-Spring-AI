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
package io.codeprimate.extensions.spring.ai.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.codeprimate.extensions.util.Utils;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.advisor.observation.DefaultAdvisorObservationConvention;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.client.observation.DefaultChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring {@link Configuration} used to enable multiple {@link ChatModel ChatModels} with a single {@link ChatClient}
 * in a Spring AI application.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.config.ChatModelConfiguration
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Configuration
@Import(ChatModelConfiguration.class)
@SuppressWarnings("unused")
public class ChatClientConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ChatClient chatClient(ChatModel chatModel,
			@Autowired(required = false) ChatClient.Builder chatClientBuilder,
			@Autowired(required = false) Consumer<ChatClient.Builder> chatClientBuilderCustomizer,
			@Autowired(required = false) AdvisorObservationConvention advisorObservationConvention,
			@Autowired(required = false) ChatClientObservationConvention chatClientObservationConvention,
			@Autowired(required = false) ObservationRegistry observationRegistry) {

		chatClientBuilder = resolveChatClientBuilder(chatClientBuilder, () ->
			chatClientBuilder(chatModel, advisorObservationConvention, chatClientObservationConvention,
				observationRegistry));

		Utils.nullSafeConsumer(chatClientBuilderCustomizer).accept(chatClientBuilder);

		return chatClientBuilder.build();
	}

	protected ChatClient.Builder chatClientBuilder(
		ChatModel chatModel,
		AdvisorObservationConvention advisorObservationConvention,
		ChatClientObservationConvention observationConvention,
		ObservationRegistry observationRegistry
	) {
		return observationRegistry != null
			? ChatClient.builder(chatModel, observationRegistry,
				resolveChatClientObservationConvention(observationConvention),
				resolveAdvisorObservationConvention(advisorObservationConvention))
			: ChatClient.builder(chatModel);
	}

	protected ChatClient.Builder resolveChatClientBuilder(ChatClient.Builder chatClientBuilder,
			Supplier<ChatClient.Builder> supplier) {

		return chatClientBuilder != null ? chatClientBuilder : supplier.get();
	}

	protected AdvisorObservationConvention resolveAdvisorObservationConvention(
			AdvisorObservationConvention advisorObservationConvention) {

		return advisorObservationConvention != null ? advisorObservationConvention
			: new DefaultAdvisorObservationConvention();
	}

	protected ChatClientObservationConvention resolveChatClientObservationConvention(
			ChatClientObservationConvention chatClientObservationConvention) {

		return chatClientObservationConvention != null ? chatClientObservationConvention
			: new DefaultChatClientObservationConvention();
	}
}
