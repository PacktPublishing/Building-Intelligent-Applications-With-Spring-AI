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

import java.util.function.Function;

import io.codeprimate.extensions.spring.ai.embedding.Vector;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.springframework.ai.embedding.Embedding;

/**
 * {@link Function} interface used to compute an {@link Embedding} from an {@link Audio} {@link Fingerprint}.
 *
 * @author John Blum
 * @see Audio
 * @see Fingerprint
 * @see java.util.function.Function
 * @see io.codeprimate.extensions.spring.ai.embedding.Vector
 * @see org.springframework.ai.embedding.Embedding
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AudioFingerprintEmbeddingFunction extends Function<Fingerprint<?>, Embedding> {

	@Override
	default Embedding apply(Fingerprint<?> audioFingerprint) {
		return embed(audioFingerprint);
	}

	Embedding embed(Fingerprint<?> audioFingerprint);

	default Vector vectorize(Fingerprint<?> audioFingerprint) {
		return Vector.from(embed(audioFingerprint));
	}
}
