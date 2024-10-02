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
package com.packt.spring.ai.examples.six;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} used to demonstrate the use of an Embeddings AI model.
 *
 * @author John Blum
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
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

			String text = "What's in a name?";

			float[] vector = embeddingModel.embed(text);

			System.out.printf("prompt> %s%n", text);
			System.out.printf("Vector Dimension: %d%n", vector.length);
		};
	}

	@Bean
	ApplicationRunner vectorStoreRunner(EmbeddingModel embeddingModel) {

		return args -> {

			VectorStore vectorStore = new SimpleVectorStore(embeddingModel);

			List<Document> documents = List.of(
				buildDocument("EddieVedder", "Do I deserve to be, is that the question, and if so,"
					+ " if so, who answers, who answers"),
				buildDocument("WilliamShakespeare", "To be, or not to be, that is the question.")
			);

			vectorStore.accept(documents);

			SearchRequest searchRequest = SearchRequest.query("is the question").withSimilarityThreshold(0.5d);

			List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

			System.out.printf("Similar Documents to [\"%s\"]: %s%n%n", searchRequest.getQuery(),
				similarDocuments.stream().map(Document::getId).toList());
		};
	}

	private Document buildDocument(String id, String content) {

		return Document.builder()
			.withContent(content)
			.withId(id)
			.build();
	}
}
