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
package com.packt.spring.ai.examples.converter;

import java.util.Comparator;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@link SpringBootApplication} using spring AI with Ollama {@literal llama3.2} model) to demonstrate
 * {@link StructuredOutputConverter StructuredOutputConverters}.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.messages.UserMessage
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.converter.StructuredOutputConverter
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class StructuredOutputConverterApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(StructuredOutputConverterApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	@SuppressWarnings("all")
	ApplicationRunner programRunner(ChatClient chatClient) {

		return args -> {

			String prompt = "List the top 5 golfers by name and major wins";

			ParameterizedTypeReference<List<Golfer>> type = new ParameterizedTypeReference<List<Golfer>>() { };

			BeanOutputConverter<List<Golfer>> tennisPlayersConverter = new BeanOutputConverter<>(type);

			List<Golfer> tennisPlayers = chatClient.prompt()
				.messages(new UserMessage(prompt))
				.call()
				.entity(tennisPlayersConverter);

			tennisPlayers.stream()
				.sorted(Comparator.comparingInt(Golfer::getMajorWins).reversed())
				.map(Golfer::toString)
				.forEach(System.out::println);
		};
	}

	@Getter
	@NoArgsConstructor
	@EqualsAndHashCode(of = "name")
	static class Golfer {

		private String name;
		private Integer majorWins;

		@Override
		public String toString() {
			return "%s, %d major wins".formatted(getName(), getMajorWins());
		}
	}
}
