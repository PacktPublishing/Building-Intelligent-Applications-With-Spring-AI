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
package io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample;

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.ffmpeg.FFProbe;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for {@link AudioFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class AudioFormatBuilder {

	protected static final int BITS_PER_BYTE = 8;

	public static AudioFormatBuilder from(Audio audio) {
		return new AudioFormatBuilder(audio);
	}

	private final AtomicReference<AudioFormat> audioFormat = new AtomicReference<>();

	private final Audio audio;

	AudioFormatBuilder(Audio audio) {
		Assert.notNull(audio, "Audio is required");
		this.audio = audio;
	}

	public AudioFormatBuilder with(AudioFormat baseAudioFormat) {
		this.audioFormat.set(baseAudioFormat);
		return this;
	}

	public AudioFormatBuilder with(AudioInputStream inputStream) {
		return with(inputStream.getFormat());
	}

	protected int getAudioChannels() {
		return getAudioFormat().getChannels();
	}

	protected AudioFormat.Encoding getAudioEncoding() {
		return getAudioFormat().getEncoding();
	}

	protected AudioFormat getAudioFormat() {
		return this.audioFormat.updateAndGet(this::resolveAudioFormat);
	}

	protected float getFrameRate() {
		return getSampleRate();
	}

	protected int getFrameSize() {

		int channels = getAudioChannels();
		int sampleSizeInBits = getSampleSizeInBits();

		return isSpecified(sampleSizeInBits) || isSpecified(channels)
			? (sampleSizeInBits + 7) / BITS_PER_BYTE * channels
			: AudioSystem.NOT_SPECIFIED;
	}

	protected float getSampleRate() {
		return getAudioFormat().getSampleRate();
	}

	protected int getSampleSizeInBits() {

		int sampleSizeInBits = getAudioFormat().getSampleSizeInBits();

		if (isNotSpecified(sampleSizeInBits)) {

			FFProbe.Format probeFormat = probeAudioFormat(getAudio());

			int audioSizeInBytes = probeFormat.size();
			int audioSizeInBits = audioSizeInBytes * BITS_PER_BYTE;
			int audioDurationInSeconds = asInt(probeFormat.getDuration().toSeconds());
			int sampleRate = asInt(getAudioFormat().getSampleRate()); // samples per second
			int totalSamples = audioDurationInSeconds * sampleRate;

			sampleSizeInBits = audioSizeInBits / totalSamples;
		}

		return sampleSizeInBits;
	}

	protected boolean isBigEndian() {
		return getAudioFormat().isBigEndian();
	}

	protected boolean isNotSpecified(int audioValue) {
		return !isSpecified(audioValue);
	}

	protected boolean isSpecified(int audioValue) {
		return Math.max(audioValue, AudioSystem.NOT_SPECIFIED) > 0;
	}

	public AudioFormat build() {
		return new AudioFormat(getAudioEncoding(), getSampleRate(), getSampleSizeInBits(), getAudioChannels(),
			getFrameSize(), getFrameRate(), isBigEndian());
	}

	private FFProbe.Format probeAudioFormat(Audio audio) {
		return new FFProbe().showFormat(audio);
	}

	private AudioFormat queryAudioFormat(Audio audio) {

		AudioInputStream inputStream = ExceptionThrowingSupplier.getSafely(() ->
			AudioSystem.getAudioInputStream(audio.inputStream()));

		return inputStream.getFormat();
	}

	private AudioFormat resolveAudioFormat(AudioFormat audioFormat) {
		return audioFormat != null ? audioFormat : queryAudioFormat(getAudio());
	}
}
