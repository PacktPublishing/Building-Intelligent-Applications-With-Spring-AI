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

import org.cp.elements.lang.Assert;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.vectorstore.SimpleVectorStore;

/**
 * Abstract Data Type (ADT) and Java record modeling an AI vector,
 * or {@link Embedding} computed from an AI Embedding Model.
 *
 * @param vector floating-point array containing the data of the vector (embedding).
 * @see org.springframework.ai.embedding.Embedding
 */
@SuppressWarnings("unused")
public record Vector(float[] vector) {

	public static Vector from(float[] vector) {
		return new Vector(vector);
	}

	public static Vector from(Embedding embedding) {
		Assert.notNull(embedding, "Embedding is required");
		return new Vector(embedding.getOutput());
	}

	public Vector {
		Assert.notNull(vector, "Vector (floating-point array) is required");
	}

	public int dimensions() {
		return vector().length;
	}

	public Embedding embedding() {
		return new Embedding(vector(), 0);
	}

	public double similarityTo(Vector that) {
		float[] vectorX = this.vector();
		float[] vectorY = that.vector();
		return SimpleVectorStore.EmbeddingMath.cosineSimilarity(vectorX, vectorY);
	}
}
