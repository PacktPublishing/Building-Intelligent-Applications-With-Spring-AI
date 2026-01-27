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

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.Fingerprint;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.SpectralPeakProcessor;

/**
 * {@link AudioFingerprintFunction} implementation using a {@literal Spectrogram} algorithm
 * provided by the {@literal TarsosDSP} library.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFingerprintFunction
 * @see be.tarsos.dsp.AudioDispatcher
 * @see be.tarsos.dsp.SpectralPeakProcessor
 * @see <a href="https://github.com/JorenSix/TarsosDSP">TarsosDSP</a>
 * @since 0.1.0
 */
public class SpectrogramAudioFingerprintFunction implements AudioFingerprintFunction<float[]> {

	protected static final int DEFAULT_FAST_FOURIER_TRANSFORM_SIZE = 1024;
	protected static final int DEFAULT_NUMBER_OF_PEAKS = 256;

	protected int getFastFourierTransformSize() {
		return DEFAULT_FAST_FOURIER_TRANSFORM_SIZE;
	}

	protected int getNumberOfPeaks() {
		return DEFAULT_NUMBER_OF_PEAKS;
	}

	// TODO: reevaluate implementation for correctness
	@Override
	public Fingerprint<float[]> compute(Audio audio) {

		int fftSize = getFastFourierTransformSize();
		int overlap = fftSize / 2;
		int sampleRate = NumberUtils.asInt(audio.getFormat().getSampleRate());

		SpectralPeakProcessor spectralPeakProcessor = new SpectralPeakProcessor(fftSize, overlap, sampleRate);

		AudioDispatcher dispatcher = AudioDispatcherBuilder.from(audio)
			.register(spectralPeakProcessor)
			.build();

		dispatcher.run();

		float[] frequencyEstimates = spectralPeakProcessor.getFrequencyEstimates();
		float[] magnitudes = spectralPeakProcessor.getMagnitudes();
		float[] noiseFloor = SpectralPeakProcessor.calculateNoiseFloor(magnitudes, 8, 1.0f);

		List<Integer> localMaxima = SpectralPeakProcessor.findLocalMaxima(magnitudes, noiseFloor);

		List<SpectralPeakProcessor.SpectralPeak> spectralPeaks = SpectralPeakProcessor.findPeaks(
			magnitudes,
			frequencyEstimates,
			localMaxima,
			getNumberOfPeaks(),
			Integer.MAX_VALUE
		);

		List<Float> spectralPeakFrequencies = spectralPeaks.stream()
			.map(SpectralPeakProcessor.SpectralPeak::getRefFrequencyInHertz)
			.toList();

		// TODO: Algorithm is incomplete!

		return toFingerprint(toPrimitiveFloatArray(spectralPeakFrequencies));
	}

	@SuppressWarnings("unused")
	private float[] flatten(float[][] twoDimensionalArray) {

		float[] array = new float[twoDimensionalArray.length * twoDimensionalArray[0].length];

		for (int x = 0; x < twoDimensionalArray.length; x++) {
			for (int y = 0; y < twoDimensionalArray[x].length; y++) {
				int index = x * y + y;
				array[index] = twoDimensionalArray[x][y];
			}
		}

		return array;
	}

	private float[] toPrimitiveFloatArray(List<Float> list) {

		float[] array = new float[list.size()];

		for (int index = 0; index < array.length; index++) {
			array[index] = list.get(index);
		}

		return array;
	}

	private Fingerprint<float[]> toFingerprint(float[] array) {

		return new Fingerprint<>() {

			@Override
			public float[] get() {
				return array;
			}

			@Override
			public byte[] getData() {

				float[] array = get();
				byte[] data = new byte[array.length];

				for (int index = 0; index < array.length; index++) {
					data[index] = Float.valueOf(array[index]).byteValue();
				}

				return data;
			}
		};
	}
}
