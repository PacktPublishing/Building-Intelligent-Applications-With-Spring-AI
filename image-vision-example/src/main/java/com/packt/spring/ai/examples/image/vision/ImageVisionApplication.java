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
package com.packt.spring.ai.examples.image.vision;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.util.Utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal llama3.2} model) to evaluate an image.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ImageVisionApplication {

	private static final int IMAGE_WIDTH = 795;
	private static final int IMAGE_HEIGHT = 600;

	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(ImageVisionApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		printAiProviderName(chatModel);
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	@SuppressWarnings("all")
	ApplicationRunner programRunner(ChatClient chatClient) {

		return applicationArguments -> {

			String instructions = """
				Evaluate the given image and find the exact location of Waldo
				Describe the precise location in detail
			""";
			// Generate a new image from the given image and circle the person dressed like Batmen in red.

			//Resource imageResource = new ClassPathResource("/WheresWaldoCityScene.png");
			Resource imageResource = new ClassPathResource("/WheresWaldoGardenScene.png");
			//Resource imageResource = new ClassPathResource("/WheresWaldoWinterScene.png");

			Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, imageResource);

			Consumer<ChatClient.PromptUserSpec> userPrompt =
				userSpec -> userSpec.text(instructions).media(imageMedia);

			ChatResponse response = chatClient.prompt()
				.advisors(newLoggingAdvisor())
				.user(userPrompt)
				.call()
				.chatResponse();

			List<Generation> generations = response.getResults();

			for (Generation generation : generations) {

				AssistantMessage message = generation.getOutput();

				print("%n%nAI> %s%n", message.getText());

				List<Media> mediaList = message.getMedia();

				if (Utils.isNotEmpty(mediaList)) {
					Media generatedMedia = mediaList.get(0);
					byte[] generatedImageData = generatedMedia.getDataAsByteArray();
					File generatedImageFile = new File(System.getProperty("user.dir"), "generatedImage.jpg");
					try (FileOutputStream out = new FileOutputStream(generatedImageFile)) {
						out.write(generatedImageData);
						out.flush();
					}
				}
				else {
					print("No image generated");
				}
			}
		};
	}

	private void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	private void printAiProviderName(ChatModel chatModel) {
		print("AI provider [%s]%n", SpringAiProvider.findByModel(chatModel).map(AiProvider::getName).orElse(null));
	}

	private Advisor newLoggingAdvisor() {
		return new SimpleLoggerAdvisor(requestToString(), responseToString(), Ordered.HIGHEST_PRECEDENCE);
	}

	private Function<ChatClientRequest, String> requestToString() {

		return request -> {
			String contents = request.prompt().getContents();
			ChatOptions chatOptions = request.prompt().getOptions();
			return "Prompt [%s]; AI model [%s]".formatted(contents, chatOptions.getModel());
		};
	}

	private Function<ChatResponse, String> responseToString() {

		return response -> {
			List<Generation> generations = response.getResults();
			return "Generation count [%d]".formatted(generations.size());
		};
	}
}
