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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.springframework.util.Assert;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.mfcc.MFCC;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@literal Builder} for {@link AudioDispatcher}
 *
 * @author John Blum
 * @see Audio
 * @see AudioDispatcher
 * @see AudioProcessor
 * @see MFCC
 * @since 0.1.0
 */
@SuppressWarnings("unused")
@Getter(AccessLevel.PROTECTED)
public class AudioDispatcherBuilder {

	protected static final int DEFAULT_AUDIO_BUFFER_OVERLAP = 1024;
	protected static final int DEFAULT_AUDIO_BUFFER_SIZE = 2048;

	public static AudioDispatcherBuilder from(Audio audio) {
		return new AudioDispatcherBuilder(audio);
	}

	private Integer audioBufferOverlap;
	private Integer audioBufferSize;
	private Integer numberOfCepstrumCoefficients;
	private Integer numberOfMelFilters;

	private final Audio audio;

	private final List<AudioProcessor> audioProcessors = new ArrayList<>();

	protected AudioDispatcherBuilder(Audio audio) {
		this.audio = AudioUtils.assertAudio(audio);
	}

	public int getAudioBufferOverlap() {
		Integer audioBufferOverlap = this.audioBufferOverlap;
		return audioBufferOverlap != null ? audioBufferOverlap
			: DEFAULT_AUDIO_BUFFER_OVERLAP;
	}

	public int getAudioBufferSize() {
		Integer audioBufferSize = this.audioBufferSize;
		return audioBufferSize != null ? audioBufferSize
			: DEFAULT_AUDIO_BUFFER_SIZE;
	}

	public AudioFormat getAudioFormat() {
		return AudioFormatBuilder.from(getAudio()).build();
	}

	protected int getNumberOfCepstrumCoefficients() {
		Integer numberOfCoefficients = this.numberOfCepstrumCoefficients;
		return numberOfCoefficients != null ? numberOfCoefficients
			: MfccAudioFingerprintFunction.DEFAULT_NUMBER_OF_CEPSTRUM_COEFFICIENTS;
	}

	protected int getNumberOfMelFilters() {
		Integer numberOfFilters = this.numberOfMelFilters;
		return numberOfFilters != null ? numberOfFilters
			: MfccAudioFingerprintFunction.DEFAULT_NUMBER_OF_MEL_FILTERS;
	}

	public AudioDispatcherBuilder register(AudioProcessor audioProcessor) {
		Assert.notNull(audioProcessor, "AudioProcessor is required");
		this.audioProcessors.add(audioProcessor);
		return this;
	}

	public AudioDispatcherBuilder registerMFCC(Consumer<float[]> mfccConsumer) {
		return register(newMFCC(mfccConsumer));
	}

	public AudioDispatcherBuilder withNumberOfCepstrumCoefficients(int numberOfCoefficients) {
		this.numberOfCepstrumCoefficients = numberOfCoefficients;
		return this;
	}

	public AudioDispatcherBuilder withNumberOfMelFilters(int numberOfFilters) {
		this.numberOfMelFilters = numberOfFilters;
		return this;
	}

	private MFCC newMFCC(Consumer<float[]> mfccConsumer) {

		int samplesPerFrame = getAudioBufferSize();
		float sampleRate = getAudioFormat().getSampleRate();

		return new MFCC(samplesPerFrame, sampleRate, getNumberOfCepstrumCoefficients(), getNumberOfMelFilters(),
			MfccAudioFingerprintFunction.LOWER_FREQUENCY_FILTER, MfccAudioFingerprintFunction.UPPER_FREQUENCY_FILTER) {

			@Override
			public void processingFinished() {
				super.processingFinished();
				mfccConsumer.accept(getMFCC());
			}
		};
	}

	public AudioDispatcherBuilder usingAudioBufferOverlap(Integer audioBufferOverlap) {
		this.audioBufferOverlap = audioBufferOverlap;
		return this;
	}

	public AudioDispatcherBuilder usingAudioBufferSize(Integer audioBufferSize) {
		this.audioBufferSize = audioBufferSize;
		return this;
	}

	private TarsosDSPAudioInputStream asTarsosDSPAudioInputStream(AudioInputStream audioInputStream) {
		return new JVMAudioInputStream(audioInputStream);
	}

	private AudioInputStream openInputStream(Audio audio) {
		return AudioInputStreamBuilder.from(audio).build();
	}

	public AudioDispatcher build() {

		AudioInputStream audioInputStream = openInputStream(getAudio());

		AudioDispatcher audioDispatcher = new AudioDispatcher(asTarsosDSPAudioInputStream(audioInputStream),
			getAudioBufferSize(), getAudioBufferOverlap());

		getAudioProcessors().forEach(audioDispatcher::addAudioProcessor);

		return audioDispatcher;
	}
}
