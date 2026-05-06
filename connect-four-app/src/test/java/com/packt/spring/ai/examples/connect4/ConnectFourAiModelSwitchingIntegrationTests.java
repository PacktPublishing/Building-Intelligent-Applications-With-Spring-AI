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

import static org.assertj.core.api.Assertions.assertThat;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.util.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests asserting the exchange of AI models used in the Connect Four application.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
	"spring.ai.google.genai.api-key=mockApiKey",
	"spring.ai.google.genai.location=us-west1",
	"spring.ai.google.genai.project-id=mockGeminiProjectId",
})
@ActiveProfiles({ ConnectFourApplication.CONNECT_FOUR_PROFILE, "user" })
@SuppressWarnings("unused")
public class ConnectFourAiModelSwitchingIntegrationTests {

	@Autowired
	private CompositeChatModel chatModel;

	@BeforeEach
	public void assertChatModelIsConfiguredCorrectly() {

		assertThat(this.chatModel).isNotNull();
		assertThat(toConfiguredChatModelTypes(this.chatModel))
			.containsExactlyInAnyOrder(MistralAiChatModel.class, OllamaChatModel.class, OpenAiChatModel.class,
				GoogleGenAiChatModel.class);
	}

	private Iterable<Class<?>> toConfiguredChatModelTypes(Iterable<ChatModel> chatModels) {

		return Utils.stream(chatModels)
			.map(Utils::resolveChatModel)
			.<Class<?>>map(ChatModel::getClass)
			.toList();
	}

	@Test
	void switchAiProviderModels() {

		this.chatModel.use(SpringAiProvider.OPEN_AI);

		ChatModel currentChatModel = Utils.resolveChatModel(this.chatModel.getCurrentChatModel());

		assertThat(currentChatModel.getClass()).isEqualTo(OpenAiChatModel.class);

		this.chatModel.use(SpringAiProvider.VERTEX_AI_GEMINI);
		currentChatModel = Utils.resolveChatModel(this.chatModel.getCurrentChatModel());

		assertThat(currentChatModel.getClass()).isEqualTo(GoogleGenAiChatModel.class);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ConnectFourApplication.ConnectFourConfiguration.class)
	static class TestConfiguration {

	}
}
