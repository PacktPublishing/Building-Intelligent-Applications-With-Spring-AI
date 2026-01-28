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

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.lang.NonNull;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Mock implementation of Spring AI's {@link EmbeddingModel}.
 *
 * @author John Blum
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class MockEmbeddingModel extends AbstractEmbeddingModel {

	private static final int DEFAULT_DIMENSIONS = 791;

	private final int dimensions;

	private final SecureRandom secureRandom;

	public MockEmbeddingModel() {
		this(DEFAULT_DIMENSIONS);
	}

	public MockEmbeddingModel(int dimensions) {
		this.dimensions = Math.max(dimensions, 1);
		this.secureRandom = new SecureRandom(UUID.randomUUID().toString().getBytes());
	}

	@Override
	public int dimensions() {
		return this.dimensions;
	}

	@Override
	public @NonNull float[] embed(@NonNull Document document) {

		float[] embedding = new float[dimensions()];

		for (int index = 0; index < embedding.length; index++) {
			embedding[index] = secureRandom.nextFloat();
		}

		return embedding;
	}
}
