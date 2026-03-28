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
package org.springframework.ai.vectorstore;

import java.util.Collections;
import java.util.Map;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.IdentityException;
import org.cp.elements.lang.ImmutableIdentifiable;
import org.springframework.ai.content.Content;
import org.springframework.ai.document.Document;

/**
 * Abstract Data Type (ADT) modeling {@link VectorStore} {@link Content}.
 *
 * @author John Blum
 * @see org.cp.elements.lang.ImmutableIdentifiable
 * @see org.springframework.ai.content.Content
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
public interface VectorStoreContent extends ImmutableIdentifiable<String>, Content {

	float[] EMPTY_EMBEDDING = new float[0];

	static VectorStoreContent from(Document document, float[] embedding) {

		Assert.notNull(document, "Document is required");
		Assert.notNull(embedding, "Embedding is required");

		return new VectorStoreContent() {

			@Override
			public float[] getEmbedding() {
				return embedding;
			}

			@Override
			public String getId() {
				return document.getId();
			}

			@Override
			public Map<String, Object> getMetadata() {
				return document.getMetadata();
			}

			@Override
			public String getText() {
				return document.getText();
			}

			@Override
			public Document toDocument(Double score) {
				return document;
			}
		};
	}

	default Object get(String key) {
		return getMetadata().get(key);
	}

	default float[] getEmbedding() {
		return EMPTY_EMBEDDING;
	}

	@Override
	default String getId() {
		throw new IdentityException("ID not set");
	}

	@Override
	default Map<String, Object> getMetadata() {
		return Collections.emptyMap();
	}

	default Document toDocument(Double score) {
		return Document.builder()
			.text(getText())
			.metadata(Collections.unmodifiableMap(getMetadata()))
			.score(score)
			.build();
	}
}
