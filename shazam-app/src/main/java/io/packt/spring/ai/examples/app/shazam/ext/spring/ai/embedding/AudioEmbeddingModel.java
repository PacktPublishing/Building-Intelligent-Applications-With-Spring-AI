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
package io.packt.spring.ai.examples.app.shazam.ext.spring.ai.embedding;

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp.MfccAudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.DocumentStore;

import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import be.tarsos.dsp.AudioDispatcher;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link EmbeddingModel} implementation based on a {@link AudioFingerprintFunction} algorithm
 * used to embed {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioDispatcher
 * @see AudioFingerprintFunction
 * @see Document
 * @see DocumentStore
 * @see Embedding
 * @see EmbeddingModel
 * @see Media
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class AudioEmbeddingModel implements EmbeddingModel {

	public static final int DEFAULT_VECTOR_DIMENSIONS = 37;
	private static final int DEFAULT_INDEX = 0;

	private static final String EMPTY_STRING = "";

	private final AudioFingerprintFunction audioFingerprintFunction;

	private final DocumentStore documentStore;

	AudioEmbeddingModel(DocumentStore documentStore) {
		this(new MfccAudioFingerprintFunction(DEFAULT_VECTOR_DIMENSIONS), documentStore);
	}

	public AudioEmbeddingModel(AudioFingerprintFunction audioFingerprintFunction, DocumentStore documentStore) {
		Assert.notNull(audioFingerprintFunction, "AudioFingerprintFunction is required");
		Assert.notNull(documentStore, "DocumentStore is required");
		this.audioFingerprintFunction = audioFingerprintFunction;
		this.documentStore = documentStore;
	}

	@Override
	public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {

		List<Document> documents = toDocuments(request);

		List<Embedding> embeddings = documents.stream()
			.map(this::embed)
			.map(this::toEmbedding)
			.toList();

		return toEmbeddingResponse(embeddings);
	}

	@Override
	public int dimensions() {
		return DEFAULT_VECTOR_DIMENSIONS;
	}

	@Override
	public @NonNull float[] embed(@NonNull Document document) {
		Audio audio = toAudio(document);
		return getAudioFingerprintFunction().compute(audio);
	}

	private Audio toAudio(Document document) {

		Assert.notNull(document, "Document is required");

		if (document instanceof AbstractDocumentStore.AudioDocument audioDocument) {
			return audioDocument.getAudio();
		}
		else {
			Media media = document.getMedia();
			byte[] data = media.getDataAsByteArray();
			return Audio.from(data);
		}
	}

	private List<Document> toDocuments(EmbeddingRequest request) {

		List<String> instructions = request.getInstructions();

		return instructions.stream()
			.map(getDocumentStore()::get)
			.toList();
	}

	private Embedding toEmbedding(float[] vector) {
		return toEmbedding(vector, DEFAULT_INDEX);
	}

	private Embedding toEmbedding(float[] vector, int index) {
		return new Embedding(vector, index);
	}

	private EmbeddingResponse toEmbeddingResponse(List<Embedding> embeddings) {
		return new EmbeddingResponse(embeddings, withEmbeddingResponseMetadata());
	}

	private EmbeddingResponseMetadata withEmbeddingResponseMetadata() {
		return new EmbeddingResponseMetadata(getAudioFingerprintFunction().getName(), new EmptyUsage());
	}
}
