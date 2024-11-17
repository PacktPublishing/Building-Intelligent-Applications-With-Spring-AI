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
package com.packt.spring.ai.examples.image.generation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} using Spring AI with OpenAI ({@literal DALL-E-2} model) to generate an {@link Image}.
 *
 * @author John Blum
 * @see org.springframework.ai.image.Image
 * @see org.springframework.ai.image.ImageGeneration
 * @see org.springframework.ai.image.ImageModel
 * @see org.springframework.ai.image.ImagePrompt
 * @see org.springframework.ai.image.ImageResponse
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ImageGenerationApplication {

	private static final int WIDTH = 512;
	private static final int HEIGHT = 512;

	private static final String IMAGE_DIRECTORY_PATHNAME = System.getProperty("user.dir");
	private static final String IMAGE_FILENAME = "cat.png";
	private static final String IMAGE_SIZE = "512x512";
	private static final String PNG = "png";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(ImageGenerationApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(ImageModel imageModel) {

		return args -> {

			String instructions = """
					Generate an image of an upside down cat suspended in midair,
					parallel to the ground, with a slice of buttered bread
					directly on the cat's back.
				""";

			OpenAiImageOptions options = new OpenAiImageOptions();

			options.setWidth(WIDTH);
			options.setHeight(HEIGHT);
			options.setSize(IMAGE_SIZE);

			ImagePrompt prompt = new ImagePrompt(instructions, options);

			ImageResponse response = imageModel.call(prompt);

			ImageGeneration imageGeneration = response.getResult();

			Image image = imageGeneration.getOutput();

			String URL = image.getUrl();

			print("URL [%s]%n", URL);
			print("Image Base64 JSON [%s]%n", image.getB64Json());

			//save(image);
		};
	}

	private void print(String label, Object... arguments) {
		System.out.printf(label, arguments);
		System.out.flush();
	}

	private void save(Image image) throws IOException {

		String imageBase64Json = image.getB64Json();

		byte[] imageBytes = Base64.getDecoder().decode(imageBase64Json);

		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			File imageFilePath = new File(IMAGE_DIRECTORY_PATHNAME, IMAGE_FILENAME);
			ImageIO.write(bufferedImage, PNG, imageFilePath);
		}
	}
}
