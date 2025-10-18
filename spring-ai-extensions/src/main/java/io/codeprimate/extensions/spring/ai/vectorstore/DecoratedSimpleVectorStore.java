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
package io.codeprimate.extensions.spring.ai.vectorstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.codeprimate.extensions.spring.ai.document.DocumentNotFoundException;
import io.codeprimate.extensions.spring.ai.document.EmbeddedDocument;
import io.codeprimate.extensions.spring.ai.embedding.EmbeddingModelWrapper;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.lang.NonNull;

/**
 * Decorated {@link SimpleVectorStore} that stores {@link Document embedded documents}
 * without re-computing the embedding.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.vectorstore.SimpleVectorStore
 * @see io.codeprimate.extensions.spring.ai.document.EmbeddedDocument
 * @see io.codeprimate.extensions.spring.ai.embedding.EmbeddingModelWrapper
 * @see <a href="https://en.wikipedia.org/wiki/Decorator_pattern">Decorator Software Design Pattern</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class DecoratedSimpleVectorStore extends SimpleVectorStore {

	protected static final double SCORE = 0.0d;

	public DecoratedSimpleVectorStore(@NonNull EmbeddingModel embeddingModel) {
		super(SimpleVectorStoreBuilderSupport.builder(embeddingModel));
	}

	public DecoratedSimpleVectorStore(SimpleVectorStoreBuilder vectorStoreBuilder) {
		super(SimpleVectorStoreBuilderSupport.copy(vectorStoreBuilder));
	}

	@Override
	public void accept(@NonNull List<Document> documents) {

		List<Document> embeddedDocuments = store(documents);

		documents = reduce(documents, embeddedDocuments);

		if (Utils.isNotEmpty(documents)) {
			super.accept(documents);
		}
	}

	public void add(@NonNull Document document) {
		Assert.notNull(document, "Document to add is required");
		accept(Collections.singletonList(document));
	}

	@SuppressWarnings("all")
	public EmbeddedDocument get(String documentId) {

		SimpleVectorStoreContent vectorStoreContent = this.store.get(documentId);

		Assert.state(vectorStoreContent != null, DocumentNotFoundException.forDocumentId(documentId));

		Document document = vectorStoreContent.toDocument(SCORE);

		return EmbeddedDocument.from(document)
			.withEmbedding(vectorStoreContent.getEmbedding());
	}

	private List<Document> reduce(List<Document> source, List<Document> exclude) {
		List<Document> documents = new ArrayList<>(source);
		documents.removeAll(exclude);
		return documents;
	}

	private List<Document> store(List<Document> documents) {

		return documents.stream()
			.filter(EmbeddedDocument::isEmbeddingPresent)
			.map(EmbeddedDocument.class::cast)
			.map(this::store)
			.toList();
	}

	private Document store(EmbeddedDocument document) {
		this.store.put(document.getId(), simpleVectorStoreContent(document));
		return document;
	}

	private SimpleVectorStoreContent simpleVectorStoreContent(EmbeddedDocument document) {
		return new SimpleVectorStoreContent(document.getId(), document.getText(), document.getMetadata(),
			document.getEmbedding());
	}

	protected static class SimpleVectorStoreBuilderSupport {

		public static SimpleVectorStoreBuilder builder(EmbeddingModel embeddingModel) {
			return SimpleVectorStore.builder(EmbeddingModelWrapper.from(embeddingModel));
		}

		@SuppressWarnings("all")
		public static SimpleVectorStoreBuilder copy(SimpleVectorStoreBuilder vectorStoreBuilder) {

			Assert.notNull(vectorStoreBuilder, "SimpleVectorStoreBuilder to copy is required");

			EmbeddingModel embeddingModel = vectorStoreBuilder.getEmbeddingModel();

			SimpleVectorStoreBuilder copy = builder(embeddingModel)
				.observationRegistry(vectorStoreBuilder.getObservationRegistry());

			VectorStoreObservationConvention observationConvention =
				vectorStoreBuilder.getCustomObservationConvention();

			copy = observationConvention != null ? copy.customObservationConvention(observationConvention) : copy;

			return copy;
		}
	}
}
