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
package io.packt.spring.ai.examples.app.shazam.service;

import java.util.HashMap;
import java.util.Map;

import io.codeprimate.extensions.data.caching.SimpleCache;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.AudioSource;
import io.packt.spring.ai.examples.app.shazam.model.MediaSource;
import io.packt.spring.ai.examples.app.shazam.support.UuidGenerator;

import org.springframework.ai.document.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract base class encapsulating functionality common to all {@link DocumentStore} implementations.
 *
 * @author John Blum
 * @see DocumentStore
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractDocumentStore implements DocumentStore {

	public static DocumentStore inMemory() {

		SimpleCache<String, Document> documentCache = SimpleCache.inMemory();

		return new DocumentStore() {

			@Override
			public boolean isEmpty() {
				return documentCache.isEmpty();
			}

			@Override
			public Document get(String id) {

				if (StringUtils.hasText(id)) {
					Document document = documentCache.get(id);
					if (document != null) {
						return document;
					}
				}

				return DocumentStore.super.get(id);
			}

			@Override
			public boolean remove(Document document) {
				return document != null && documentCache.evict(document.getId()) != null;
			}

			@Override
			public long size() {
				return documentCache.size();
			}

			@Override
			public Document save(Document document) {

				Assert.notNull(document, "Document to save is required");
				Assert.hasText(document.getId(), "Document ID was not set");

				documentCache.put(document.getId(), document);

				return document;
			}
		};
	}

	public static AudioDocument newAudioDocument(AudioSource audioSource) {
		return newAudioDocument(audioSource, null, null);
	}

	public static AudioDocument newAudioDocument(AudioSource audioSource, Map<String, Object> metadata) {
		return newAudioDocument(audioSource, metadata, null);
	}

	public static AudioDocument newAudioDocument(AudioSource audioSource, String id) {
		return newAudioDocument(audioSource, null, id);
	}

	public static AudioDocument newAudioDocument(AudioSource audioSource, Map<String, Object> metadata, String id) {

		return AudioDocument.builder(audioSource)
			.with(metadata)
			.identifiedBy(id)
			.build();
	}

	@Getter(AccessLevel.PROTECTED)
	public static class AudioDocument extends Document implements AudioSource, MediaSource {

		protected static Builder builder(AudioSource audioSource) {
			return new Builder(audioSource);
		}

		private final AudioSource audioSource;

		private AudioDocument(String id, AudioSource audioSource, Map<String, Object> metadata) {
			super(id, audioSource.getAudio().getMedia(), metadata);
			this.audioSource = audioSource;
		}

		@Override
		public Audio getAudio() {
			return getAudioSource().getAudio();
		}

		// Hacky workaround to Spring AI (API) limitations thats supports text-only embeddings!
		@Override
		@SuppressWarnings("all")
		public String getText() {
			return getId();
		}

		@Override
		public boolean isText() {
			return true;
		}

		@Override
		@SuppressWarnings("all")
		public String toString() {
			return getId();
		}

		@Getter(AccessLevel.PROTECTED)
		protected static class Builder {

			private final AudioSource audioSource;

			private Map<String, Object> metadata;

			private String id;

			protected Builder(AudioSource audioSource) {
				Assert.notNull(audioSource, "AudioSource is required");
				this.audioSource = audioSource;
			}

			protected Builder identifiedBy(String id) {
				this.id = id;
				return this;
			}

			protected Builder with(Map<String, Object> metadata) {
				this.metadata = metadata;
				return this;
			}

			protected String generateId() {
				return UuidGenerator.INSTANCE.generateId(getAudioSource());
			}

			protected String resolveId() {
				String id = getId();
				return StringUtils.hasText(id) ? id : generateId();
			}

			protected Map<String, Object> resolveMetadata() {
				Map<String, Object> configuredMetadata = getMetadata();
				return configuredMetadata != null ? configuredMetadata : new HashMap<>();
			}

			protected AudioDocument build() {
				return new AudioDocument(resolveId(), getAudioSource(), resolveMetadata());
			}
		}
	}
}
