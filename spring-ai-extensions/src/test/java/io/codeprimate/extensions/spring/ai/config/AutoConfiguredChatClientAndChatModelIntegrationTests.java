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

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.springframework.ai.autoconfigure.chat.client.ChatClientAutoConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link SpringBootTest} using {@literal auto-configuration} for {@link ChatClient} and {@link ChatModel} beans.
 *
 * @author John Blum
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@SuppressWarnings("unused")
public class AutoConfiguredChatClientAndChatModelIntegrationTests {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ChatClientCustomizer chatClientCustomizer;

	@Autowired
	private ChatModel chatModel;

	@Autowired
	@Qualifier("chatModelOne")
	private ChatModel currentChatModel;

	@Autowired
	private Consumer<ChatClient.Builder> chatClientConsumer;

	@Test
	void chatClientInitialized() {
		assertThat(this.chatClient).isNotNull();
	}

	@Test
	void chatModelIsComposite() {

		assertThat(this.chatModel).isInstanceOf(CompositeChatModel.class)
			.asInstanceOf(InstanceOfAssertFactories.type(CompositeChatModel.class))
			.extracting(CompositeChatModel::getCurrentChatModel)
			.isEqualTo(this.currentChatModel);
	}

	@Test
	void chatClientBuilderConsumerCalled() {

		assertThat(this.chatClientConsumer).isNotNull();
		verify(this.chatClientConsumer, times(1)).accept(isA(ChatClient.Builder.class));
		verifyNoMoreInteractions(this.chatClientConsumer);
	}

	@Test
	void chatClientCustomizerCalled() {

		assertThat(this.chatClientCustomizer).isNotNull();
		verify(this.chatClientCustomizer, times(1)).customize(isA(ChatClient.Builder.class));
		verifyNoMoreInteractions(this.chatClientCustomizer);
	}

	@SpringBootConfiguration
	@EnableChatClient
	@Import(ChatClientAutoConfiguration.class)
	static class AutoConfiguredChatClientAndChatModelTestConfiguration {

		@Bean
		ChatModel chatModelOne() {
			return mock(ChatModel.class, "A");
		}

		@Bean
		ChatModel chatModelTwo() {
			return mock(ChatModel.class, "B");
		}

		@Bean
		ChatClientCustomizer chatClientCustomizer() {
			return mock(ChatClientCustomizer.class);
		}

		@Bean
		@SuppressWarnings("unchecked")
		Consumer<ChatClient.Builder> chatClientConsumer() {
			return mock(Consumer.class);
		}
	}
}
