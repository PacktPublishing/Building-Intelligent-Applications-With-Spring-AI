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

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Nameable;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.embedding.EmbeddingResultMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * Abstract base class encapsulating functionality common to all {@link EmbeddingModel} implementations.
 *
 * @author John Blum
 * @see org.cp.elements.lang.Nameable
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.embedding.Embedding
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @since 0.1.0
 */
public abstract class AbstractEmbeddingModel implements EmbeddingModel, Nameable<String> {

	protected static final int DEFAULT_INDEX = 0;

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {

		List<Embedding> embeddings = toDocuments(request).stream()
			.map(this::embed)
			.map(this::toEmbedding)
			.toList();

		return toEmbeddingResponse(embeddings);
	}

	protected List<Document> toDocuments(EmbeddingRequest request) {

		List<String> instructions = request.getInstructions();

		return instructions.stream()
			.map(this::toDocument)
			.toList();
	}

	protected Document toDocument(String text) {
		return Document.builder()
			.text(text)
			.build();
	}

	protected Embedding toEmbedding(float[] vector) {
		return toEmbedding(vector, DEFAULT_INDEX, this::withEmbeddingResultMetadata);
	}

	protected Embedding toEmbedding(float[] vector, int index,
			Supplier<EmbeddingResultMetadata> embeddingResultMetadata) {

		return new Embedding(vector, index, embeddingResultMetadata.get());
	}

	protected EmbeddingResponse toEmbeddingResponse(List<Embedding> embeddings) {
		return new EmbeddingResponse(embeddings);
	}

	protected EmbeddingResponseMetadata withEmbeddingResponseMetadata() {
		return new EmbeddingResponseMetadata(getName(), new EmptyUsage(), Collections.emptyMap());
	}

	protected EmbeddingResultMetadata withEmbeddingResultMetadata() {
		return EmbeddingResultMetadata.EMPTY;
	}

	protected EmbeddingResultMetadata withEmbeddingResultMetadata(Document document) {

		Assert.notNull(document, "Document is required");

		MimeType mimeType = resolveMimeType(document);
		EmbeddingResultMetadata.ModalityType modalityType = resolveModalityType(mimeType);
		Object data = resolveData(document, modalityType);

		return new EmbeddingResultMetadata(document.getId(), modalityType, mimeType, data);
	}

	protected Object resolveData(Document document, EmbeddingResultMetadata.ModalityType modalityType) {
		return EmbeddingResultMetadata.ModalityType.TEXT.equals(modalityType) ? document.getText()
			: resolveData(document);
	}

	private Object resolveData(Document document) {
		Media media = document.getMedia();
		return media != null ? media.getData() : null;
	}

	protected MimeType resolveMimeType(Document document) {
		Media media = document.getMedia();
		return media != null ? media.getMimeType() : null;
	}

	protected EmbeddingResultMetadata.ModalityType resolveModalityType(MimeType mimeType) {

		return isAudio(mimeType) ? EmbeddingResultMetadata.ModalityType.AUDIO
			: isImage(mimeType) ? EmbeddingResultMetadata.ModalityType.IMAGE
			: isVideo(mimeType) ? EmbeddingResultMetadata.ModalityType.VIDEO
			: EmbeddingResultMetadata.ModalityType.TEXT;
	}

	private boolean isAudio(MimeType mimeType) {
		return mimeTypePredicate("audio").test(mimeType);
	}

	private boolean isImage(MimeType mimeType) {

		return MimeTypeUtils.IMAGE_GIF.equals(mimeType)
			|| MimeTypeUtils.IMAGE_JPEG.equals(mimeType)
			|| MimeTypeUtils.IMAGE_PNG.equals(mimeType)
			|| mimeTypePredicate("image").equals(mimeType);
	}

	private boolean isVideo(MimeType mimeType) {
		return mimeTypePredicate("video").test(mimeType);
	}

	@SuppressWarnings("all")
	private Predicate<MimeType> mimeTypePredicate(String type) {
		return mimeType -> mimeType != null
			&& String.valueOf(mimeType.getType()).equalsIgnoreCase(type);
	}
}
