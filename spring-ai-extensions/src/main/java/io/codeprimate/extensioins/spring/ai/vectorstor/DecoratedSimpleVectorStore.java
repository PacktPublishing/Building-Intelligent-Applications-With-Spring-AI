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
package io.codeprimate.extensioins.spring.ai.vectorstor;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;

/**
 * Decorated {@link SimpleVectorStore} that stores {@link Document embedded documents} without computing
 * the embedding.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.vectorstore.SimpleVectorStore
 * @see <a href="https://en.wikipedia.org/wiki/Decorator_pattern">Decorator Software Design Pattern</a>
 */
@SuppressWarnings("unused")
public class DecoratedSimpleVectorStore extends SimpleVectorStore {

	public DecoratedSimpleVectorStore(EmbeddingModel embeddingModel) {
		super(embeddingModel);
	}

	public DecoratedSimpleVectorStore(EmbeddingModel embeddingModel, ObservationRegistry observationRegistry,
			VectorStoreObservationConvention customObservationConvention) {

		super(embeddingModel, observationRegistry, customObservationConvention);
	}

	@Override
	public void accept(List<Document> documents) {

		List<Document> embeddedDocuments = store(documents);

		documents = reduce(documents, embeddedDocuments);

		super.accept(documents);
	}

	private List<Document> reduce(List<Document> source, List<Document> exclude) {
		List<Document> documents = new ArrayList<>(source);
		documents.removeAll(exclude);
		return documents;
	}

	private List<Document> store(List<Document> documents) {

		return documents.stream()
			.filter(this::isEmbeddingPresent)
			.map(this::store)
			.toList();
	}

	private Document store(Document document) {
		this.store.put(document.getId(), document);
		return document;
	}

	private boolean isEmbeddingPresent(Document document) {
		return document != null && isNotEmpty(document.getEmbedding());
	}

	private boolean isNotEmpty(float[] array) {
		return array != null && array.length > 0;
	}
}
