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

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Nameable;

/**
 * {@link FunctionalInterface} defining a contract for encapsulating different algorithms used to compute
 * the {@literal fingerprint} ({@literal digital signature}) for {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see java.util.function.Function
 * @see org.cp.elements.lang.Nameable
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AudioFingerprintFunction extends Function<Audio, float[]>, Nameable<String> {

	@Override
	default String getName() {
		return getClass().getSimpleName();
	}

	@Override
	default float[] apply(Audio audio) {
		return compute(audio);
	}

	/**
	 * Computes a {@literal fingerprint} ({@literal digital signature}) for the given {@link Audio}.
	 *
	 * @param audio {@link Audio} used to compute the digital signature ({@literal fingerprint}.
	 * @return the computed {@literal fingerprint} of the given {@link Audio}.
	 * @see Audio
	 */
	float[] compute(Audio audio);

}
