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

import io.codeprimate.extensions.spring.ai.embedding.AbstractEmbeddingModel;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.Fingerprint;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.repo.DocumentStore;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Spring AI {@link EmbeddingModel} implementation based on a {@link AudioFingerprintFunction} algorithm
 * used to embed {@link Audio}.
 *
 * @author John Blum
 * @see AbstractEmbeddingModel
 * @see Audio
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
public class AudioEmbeddingModel extends AbstractEmbeddingModel {

	private final AudioFingerprintFunction<?> audioFingerprintFunction;

	private final DocumentStore documentStore;

	private final EmbeddingModel embeddingModel;

	public AudioEmbeddingModel(AudioFingerprintFunction<?> audioFingerprintFunction,
			DocumentStore documentStore, EmbeddingModel embeddingModel) {

		Assert.notNull(audioFingerprintFunction, "AudioFingerprintFunction is required");
		Assert.notNull(documentStore, "DocumentStore is required");
		Assert.notNull(embeddingModel, "EmbeddingModel is required");

		this.audioFingerprintFunction = audioFingerprintFunction;
		this.documentStore = documentStore;
		this.embeddingModel = embeddingModel;
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
		return getEmbeddingModel().dimensions();
	}

	@Override
	public @NonNull float[] embed(@NonNull Document document) {
		Audio audio = toAudio(document);
		Fingerprint<?> audioFingerprint = getAudioFingerprintFunction().compute(audio);
		String hexAudioFingerprint = audioFingerprint.toHexString();
		return getEmbeddingModel().embed(hexAudioFingerprint);
	}

	private @NonNull Document assertDocument(Document document) {
		Assert.notNull(document, "Document is required");
		return document;
	}

	private @NonNull Media asssertMedia(Media media) {
		Assert.notNull(media, "Media is required");
		return media;
	}

	protected Audio toAudio(Document document) {

		assertDocument(document);

		if (document instanceof AbstractDocumentStore.AudioDocument audioDocument) {
			return audioDocument.getAudio();
		}
		else {
			Media media = asssertMedia(document.getMedia());
			byte[] data = media.getDataAsByteArray();
			return Audio.from(data);
		}
	}

	@Override
	protected List<Document> toDocuments(EmbeddingRequest request) {

		List<String> instructions = request.getInstructions();

		return instructions.stream()
			.map(getDocumentStore()::get)
			.toList();
	}
}
