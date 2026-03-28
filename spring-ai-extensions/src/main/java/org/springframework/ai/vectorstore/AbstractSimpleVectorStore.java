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
import org.springframework.ai.document.Document;

/**
 * Abstract base class extending {@link SimpleVectorStore} with additional {@link VectorStore} operations.
 *
 * @author John Blum
 * @see VectorStoreContent
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.vectorstore.SimpleVectorStore
 * @since 0.1.0
 */
public abstract class AbstractSimpleVectorStore extends SimpleVectorStore {

	protected AbstractSimpleVectorStore(SimpleVectorStoreBuilder builder) {
		super(builder);
	}

	protected VectorStoreContent getContent(String documentId) {
		return newVectorStoreContent(getStore().get(documentId));
	}

	Map<String, SimpleVectorStoreContent> getStore() {
		return this.store;
	}

	protected void store(Document document, float[] embedding) {
		VectorStoreContent content = VectorStoreContent.from(document, embedding);
		store(content);
	}

	protected void store(VectorStoreContent content) {
		String id = content.getId();
		SimpleVectorStoreContent vectorStoreContent = newSimpleVectorStoreContent(content);
		getStore().put(id, vectorStoreContent);
	}

	SimpleVectorStoreContent newSimpleVectorStoreContent(VectorStoreContent content) {
		return new SimpleVectorStoreContent(content.getId(), content.getText(), content.getMetadata(),
			content.getEmbedding());
	}

	VectorStoreContent newVectorStoreContent(SimpleVectorStoreContent content) {

		Assert.notNull(content, "SimpleVectorStoreContent is required");

		return new VectorStoreContent() {

			@Override
			public float[] getEmbedding() {
				return content.getEmbedding();
			}

			@Override
			public String getId() {
				return content.getId();
			}

			@Override
			public Map<String, Object> getMetadata() {
				return Collections.unmodifiableMap(content.getMetadata());
			}

			@Override
			public String getText() {
				return content.getId();
			}

			@Override
			public Document toDocument(Double score) {
				return content.toDocument(score);
			}
		};
	}
}
