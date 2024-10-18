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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Integration Tests for {@link ChatClient} and {@link ChatModel} Spring configuration extensions.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@SpringBootTest
//@ActiveProfiles({ "test" })
@SuppressWarnings("unused")
public class ChatClientAndChatModelConfigurationIntegrationTests {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ChatModel chatModel;

	@Autowired
	@Qualifier("Y")
	private ChatModel currentChatModel;

	@Autowired
	private Consumer<ChatClient.Builder> chatClientBuilderConsumer;

	@Test
	public void chatClientIsConfiguredCorrectly() {
		assertThat(this.chatClient).isNotNull();
	}

	@Test
	void chatModelIsComposite() {

		assertThat(this.chatModel).isInstanceOf(CompositeChatModel.class);

		CompositeChatModel compositeChatModel = (CompositeChatModel) this.chatModel;

		assertThat(compositeChatModel.stream().map(Object::toString).toList())
			.containsExactlyInAnyOrder("X", "Y", "Z");

		assertThat(this.currentChatModel).isNotNull();
		assertThat(compositeChatModel.getCurrentChatModel()).isSameAs(this.currentChatModel);
	}

	@Test
	void chatClientBuilderConsumerCalled() {

		assertThat(this.chatClientBuilderConsumer).isNotNull();

		verify(this.chatClientBuilderConsumer, times(1)).accept(isA(ChatClient.Builder.class));
		verifyNoMoreInteractions(this.chatClientBuilderConsumer);
	}

	@SpringBootConfiguration
	@EnableChatClient
	static class MultipleChatModelsTestConfiguration {

		@Bean("X")
		@Order(2)
		ChatModel mockChatModelOne() {
			return mock(ChatModel.class, "X");
		}

		@Bean("Y")
		@Order(1)
		ChatModel mockChatModelTwo() {
			return mock(ChatModel.class, "Y");
		}

		@Bean("Z")
		@Order(3)
		ChatModel mockChatModelThree() {
			return mock(ChatModel.class, "Z");
		}

		@Bean
		@Profile("test")
		ChatClient testChatClient(ChatModel chatModel) {
			return ChatClient.builder(chatModel).build();
		}

		@Bean
		@SuppressWarnings("unchecked")
		Consumer<ChatClient.Builder> chatClientBuilderCustomizer() {
			return mock(Consumer.class);
		}
	}
}
