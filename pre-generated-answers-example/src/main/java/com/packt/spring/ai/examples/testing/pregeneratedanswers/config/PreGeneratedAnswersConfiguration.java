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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.PreGeneratedAnswersApplication;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.serialization.json.DocumentDeserializer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import io.codeprimate.extensions.spring.ai.vectorstore.DecoratedSimpleVectorStore;

/**
 * {@link SpringBootConfiguration} for the {@link PreGeneratedAnswersApplication}.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.ai.vectorstore.SimpleVectorStore
 * @see org.springframework.boot.SpringBootConfiguration
 * @since 0.1.0
 */
@SpringBootConfiguration
@SuppressWarnings("unused")
public class PreGeneratedAnswersConfiguration {

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer objectMapperCustomizer() {

		return jacksonObjectMapperBuilder -> {
			SimpleModule module = new SimpleModule("customDocumentDeserializerModule");
			module.addDeserializer(Document.class, new DocumentDeserializer());
			jacksonObjectMapperBuilder.modules(module);
		};
	}

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return new DecoratedSimpleVectorStore(embeddingModel);
	}
}
