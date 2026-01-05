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
import java.util.concurrent.atomic.AtomicReference;

import io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp.AudioDispatcherBuilder;
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
 * {@link EmbeddingModel} implementation based on the {@literal TarsosDSP} library used to embed {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioDispatcher
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
	private static final String EMBEDDING_MODEL = "mfcc";

	private final DocumentStore documentStore;

	public AudioEmbeddingModel(DocumentStore documentStore) {
		Assert.notNull(documentStore, "DocumentStore is required");
		this.documentStore = documentStore;
	}

	@Override
	public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {
		Document document = toDocument(request);
		float[] embedding = embed(document);
		return toEmbeddingResponse(embedding);
	}

	@Override
	public int dimensions() {
		return DEFAULT_VECTOR_DIMENSIONS;
	}

	@Override
	public @NonNull float[] embed(@NonNull Document document) {

		AtomicReference<float[]> mfcc = new AtomicReference<>();

		Audio audio = toAudio(document);

		AudioDispatcher audioDispatcher = AudioDispatcherBuilder.from(audio)
			.withNumberOfCoefficients(dimensions())
			.registerMFCC(mfcc::set)
			.build();

		audioDispatcher.run();

		return mfcc.get();
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

	private Document toDocument(EmbeddingRequest request) {
		List<String> instructions = request.getInstructions();
		String id = String.join(EMPTY_STRING, instructions);
		return getDocumentStore().get(id);
	}

	private Embedding toEmbedding(float[] vector) {
		return toEmbedding(vector, DEFAULT_INDEX);
	}

	private Embedding toEmbedding(float[] vector, int index) {
		return new Embedding(vector, index);
	}

	private EmbeddingResponse toEmbeddingResponse(float[] vector) {
		Embedding embedding = toEmbedding(vector);
		List<Embedding> embeddings = List.of(embedding);
		return new EmbeddingResponse(embeddings, withEmbeddingResponseMetadata());
	}

	private EmbeddingResponseMetadata withEmbeddingResponseMetadata() {
		return new EmbeddingResponseMetadata(EMBEDDING_MODEL, new EmptyUsage());
	}
}
