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
package io.packt.spring.ai.examples.app.shazam.dsp;

import io.codeprimate.extensions.spring.ai.embedding.Vector;

import org.cp.elements.lang.Assert;
import org.springframework.ai.embedding.Embedding;

/**
 * Default implementation of the {@link AudioFingerprintEmbeddingFunction}.
 *
 * @author John Blum
 * @see Vector
 * @see AudioFingerprintEmbeddingFunction
 * @see org.springframework.ai.embedding.Embedding
 * @since 0.1.0
 */
public class DefaultAudioFingerprintEmbeddingFunction implements AudioFingerprintEmbeddingFunction {

	@Override
	public Embedding embed(Fingerprint<?> audioFingerprint) {

		Assert.notNull(audioFingerprint, "Audio Fingerprint is required");

		byte[] data = audioFingerprint.getData();
		float[] vector = new float[data.length];

		for (int index = 0; index < data.length; index++) {
			byte datum = data[index];
			vector[index] = Byte.valueOf(datum).floatValue();
		}

		return Vector.from(vector).embedding();
	}
}
