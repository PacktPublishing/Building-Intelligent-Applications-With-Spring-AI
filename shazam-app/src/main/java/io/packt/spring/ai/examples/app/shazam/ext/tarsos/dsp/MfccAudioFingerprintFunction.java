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
package io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp;

import java.util.concurrent.atomic.AtomicReference;

import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;

import be.tarsos.dsp.AudioDispatcher;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioFingerprintFunction} implementation using the {@literal Mel-frequency Cepstrum Coefficients (MFCC)}
 * algorithm provided by the {@literal TarsosDSP} library.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFingerprintFunction
 * @see be.tarsos.dsp.AudioDispatcher
 * @see be.tarsos.dsp.mfcc.MFCC
 * @see <a href="https://github.com/JorenSix/TarsosDSP">TarsosDSP</a>
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class MfccAudioFingerprintFunction implements AudioFingerprintFunction {

	protected static final int DEFAULT_NUMBER_OF_CEPSTRUM_COEFFICIENTS = 30;
	protected static final int DEFAULT_NUMBER_OF_MEL_FILTERS = 30;

	protected static final float LOWER_FREQUENCY_FILTER = 35.0f; // Low range for humans is 20-50 Hz
	protected static final float UPPER_FREQUENCY_FILTER = 20_000.0f; // High range for human is 20 kHz

	public static final String NAME = "MFCC";

	private final int numberOfCepstrumCoefficients;
	private final int numberOfMelFilters;

	public MfccAudioFingerprintFunction() {
		this(DEFAULT_NUMBER_OF_CEPSTRUM_COEFFICIENTS);
	}

	public MfccAudioFingerprintFunction(int numberOfCepstrumCoefficients) {
		this(numberOfCepstrumCoefficients, DEFAULT_NUMBER_OF_MEL_FILTERS);
	}

	public MfccAudioFingerprintFunction(int numberOfCepstrumCoefficients, int numberOfMelFilters) {

		Assert.isTrue(numberOfCepstrumCoefficients > 0, "Number of coefficients [%d] must be greater than 0",
			numberOfCepstrumCoefficients);

		Assert.isTrue(numberOfMelFilters > -1, "Number of Mel Filters [%d] must be greater than equal 0",
			numberOfMelFilters);

		this.numberOfCepstrumCoefficients = numberOfCepstrumCoefficients;
		this.numberOfMelFilters = numberOfMelFilters;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public float[] compute(Audio audio) {

		AtomicReference<float[]> mfcc = new AtomicReference<>();

		AudioDispatcher audioDispatcher = AudioDispatcherBuilder.from(audio)
			.withNumberOfCepstrumCoefficients(getNumberOfCepstrumCoefficients())
			.withNumberOfMelFilters(getNumberOfMelFilters())
			.registerMFCC(mfcc::set)
			.build();

		audioDispatcher.run();

		return mfcc.get();
	}
}
