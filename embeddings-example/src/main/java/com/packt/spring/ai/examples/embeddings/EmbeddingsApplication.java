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
package com.packt.spring.ai.examples.embeddings;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} demonstrating Spring AI's Embeddings Model API.
 *
 * @author John Blum
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class EmbeddingsApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(EmbeddingsApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(EmbeddingModel embeddingModel) {

		return args -> {

			String text = "To be, or not to be, that is the question.";

			float[] vector = embeddingModel.embed(text);

			System.out.printf("prompt> %s%n", text);
			System.out.printf("Vector Dimension: %d%n", vector.length);
		};
	}
}
