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
package com.packt.spring.ai.examples.chat.model.options;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} using Spring AI with OpenAI and ChatGPT ({@literal gpt-4o} model) to demonstrate
 * the effects by configuring different {@link ChatOptions}.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.chat.prompt.ChatOptions
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ChatModelOptionsApplication {

	private static final int GENERATION_COUNT = 8;
	private static final int SEED = 533081924;

	public static void main(String[] args) {

		new SpringApplicationBuilder(ChatModelOptionsApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(ChatModel chatModel) {

		return args -> {

			int topK = Integer.parseInt(args.getNonOptionArgs().get(0));
			double topP = Double.parseDouble(args.getNonOptionArgs().get(1));

			String userPrompt = "Finish the sentence, 'A long, long time ago in a...'";
			//String userPrompt = "Finish the sentence, 'If you don't eat your fruits and vegetables, you will...'";

			OllamaOptions chatOptions = OllamaOptions.builder()
				//.withSeed(SEED)
				.withTopK(topK)
				.withTopP(topP)
				.build();

			Prompt prompt = new Prompt(userPrompt, chatOptions);

			print("Seed [%s] | Top K [%s] | Top P [%s]%n",
				chatOptions.getSeed(), chatOptions.getTopK(), chatOptions.getTopP());
			print("user> %s%n", getContent(prompt));

			Map<String, Integer> generationCount = new HashMap<>();

			for (int count = GENERATION_COUNT; count > 0; count--) {
				String response = getContent(chatModel.call(prompt));
				generationCount.compute(response.toLowerCase(), (key, value) -> value == null ? 1 : ++value);
			}

			generationCount.forEach((response, count) -> System.out.printf("ai [%d]> %s%n", count, response));
		};
	}

	private String getContent(ChatResponse response) {
		return response.getResult().getOutput().getContent();
	}

	private String getContent(Prompt prompt) {
		return prompt.getContents();
	}

	private void print(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}
}
