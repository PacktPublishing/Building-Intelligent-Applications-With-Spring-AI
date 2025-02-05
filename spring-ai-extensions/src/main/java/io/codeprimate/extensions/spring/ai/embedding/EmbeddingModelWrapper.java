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
package io.codeprimate.extensions.spring.ai.embedding;

import io.codeprimate.extensions.spring.ai.document.EmbeddedDocument;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.lang.NonNull;

/**
 * {@link EmbeddingModel} implementation wrapping and decorating a given {@link EmbeddingModel}
 * with enhanced functionality.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see io.codeprimate.extensions.spring.ai.document.EmbeddedDocument
 * @see <a href="https://en.wikipedia.org/wiki/Decorator_pattern">Decorator Software Design Pattern</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class EmbeddingModelWrapper implements EmbeddingModel {

	public static EmbeddingModelWrapper from(@NonNull EmbeddingModel embeddingModel) {
		return embeddingModel instanceof EmbeddingModelWrapper wrapper ? wrapper
			: new EmbeddingModelWrapper(embeddingModel);
	}

	private final EmbeddingModel embeddingModel;

	public EmbeddingModelWrapper(@NonNull EmbeddingModel embeddingModel) {
		this.embeddingModel = ObjectUtils.requireObject(embeddingModel, "EmbeddingModel to wrap is required");
	}

	protected EmbeddingModel getEmbeddingModel() {
		return this.embeddingModel;
	}

	@Override
	public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {
		return getEmbeddingModel().call(request);
	}

	@Override
	public @NonNull float[] embed(@NonNull Document document) {
		return getEmbeddingModel().embed(document);
	}

	public EmbeddedDocument embedThenReturn(@NonNull Document document) {
		Assert.notNull(document, "Document to embed is required");
		float[] embedding = embed(document);
		return EmbeddedDocument.from(document).withEmbedding(embedding);
	}
}
