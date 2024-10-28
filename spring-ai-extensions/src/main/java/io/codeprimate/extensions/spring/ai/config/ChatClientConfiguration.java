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

import java.util.List;
import java.util.function.Consumer;

import io.codeprimate.extensions.util.Utils;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.client.observation.DefaultChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring {@link Configuration} used to enable multiple {@link ChatClient ChatClients} in a Spring AI application.
 *
 * @author John Blum
 * @see io.micrometer.observation.ObservationRegistry
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.client.observation.ChatClientObservationConvention
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 */
@Configuration
@SuppressWarnings("unused")
public class ChatClientConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ChatClient chatClient(ChatModel chatModel,
			@Autowired(required = false) Consumer<ChatClient.Builder> chatClientBuilderCustomizer,
			@Autowired(required = false) ChatClientObservationConvention observationConvention,
			@Autowired(required = false) ObservationRegistry observationRegistry) {

		ChatClient.Builder chatClientBuilder = chatClientBuilder(chatModel, observationRegistry, observationConvention);

		Utils.nullSafeConsumer(chatClientBuilderCustomizer).accept(chatClientBuilder);

		return chatClientBuilder.build();
	}

	protected ChatClient.Builder chatClientBuilder(ChatModel chatModel, ObservationRegistry observationRegistry,
			ChatClientObservationConvention observationConvention) {

		return observationRegistry != null
			? ChatClient.builder(chatModel, observationRegistry, resolveObservationConvention(observationConvention))
			: ChatClient.builder(chatModel);
	}

	protected ChatClientObservationConvention resolveObservationConvention(
			ChatClientObservationConvention chatClientObservationConvention) {

		return chatClientObservationConvention != null ? chatClientObservationConvention
			: new DefaultChatClientObservationConvention();
	}

	@Bean
	@Primary
	public CompositeChatModel compositeChatModel(List<ChatModel> chatModels) {
		return CompositeChatModel.of(chatModels);
	}
}
