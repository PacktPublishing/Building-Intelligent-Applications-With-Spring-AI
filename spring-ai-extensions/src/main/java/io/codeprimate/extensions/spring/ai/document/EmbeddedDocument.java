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
package io.codeprimate.extensions.spring.ai.document;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.Media;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * Spring AI {@link Document} extension that stores the {@literal embedding}
 * computed from the {@link Document#getText() contents} of this {@link Document}
 * using an {@link EmbeddingModel}.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.Embedding
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.model.Media
 * @since 0.1.0
 */
@SuppressWarnings("all")
public class EmbeddedDocument extends Document {

	private static final int EMBEDDING_INDEX = 0;

	public static EmbeddedDocument from(@NonNull Document document) {

		Assert.notNull(document, "Document to copy is required");

		String id = document.getId();
		String text = document.getText();

		Map<String, Object> metadata = document.getMetadata();

		boolean hasText = StringUtils.hasText(text);

		return hasText ? new EmbeddedDocument(id, text, metadata)
			: new EmbeddedDocument(id, document.getMedia(), metadata);
	}
	private volatile float[] embedding;

	public static boolean isEmbeddingPresent(Document document) {
		return document instanceof EmbeddedDocument embeddedDocument && embeddedDocument.isEmbeddingPresent();
	}

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public EmbeddedDocument(@JsonProperty("content") String content) {
		super(content);
	}

	public EmbeddedDocument(String content, Map<String, Object> metadata) {
		super(content, metadata);
	}

	public EmbeddedDocument(String id, String content, Map<String, Object> metadata) {
		super(id, content, metadata);
	}

	public EmbeddedDocument(Media media, Map<String, Object> metadata) {
		super(media, metadata);
	}

	public EmbeddedDocument(String id, Media media, Map<String, Object> metadata) {
		super(id, media, metadata);
	}

	public float[] getEmbedding() {
		return this.embedding;
	}

	public Embedding getEmbeddingWrapper() {
		return new Embedding(getEmbedding(), EMBEDDING_INDEX);
	}

	public boolean isEmbeddingPresent() {
		float[] embedding = getEmbedding();
		return embedding != null && embedding.length > 0;
	}

	public EmbeddedDocument withEmbedding(float[] embedding) {
		this.embedding = embedding;
		return this;
	}
}
