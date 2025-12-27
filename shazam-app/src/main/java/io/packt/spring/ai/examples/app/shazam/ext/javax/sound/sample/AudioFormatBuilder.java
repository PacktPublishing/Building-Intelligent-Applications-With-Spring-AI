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

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.BITS_PER_BYTE;
import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.packt.spring.ai.examples.app.shazam.ext.ffmpeg.FFProbe;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for {@link AudioFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFormat
 * @see org.cp.elements.lang.Builder
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class AudioFormatBuilder implements Builder<AudioFormat> {

	public static AudioFormatBuilder from(Audio audio) {
		return new AudioFormatBuilder(audio);
	}

	private final AtomicReference<AudioFormat> audioFormat = new AtomicReference<>();

	private final Audio audio;

	private final Map<String, Object> audioProperties = new HashMap<>();

	protected AudioFormatBuilder(Audio audio) {
		Assert.notNull(audio, "Audio is required");
		this.audio = audio;
	}

	public AudioFormatBuilder copy(AudioFormat audioFormat) {
		this.audioFormat.set(audioFormat);
		this.audioProperties.putAll(audioFormat.properties());
		return this;
	}

	public AudioFormatBuilder copyFormatFrom(AudioInputStream audioInputStream) {
		return copy(audioInputStream.getFormat());
	}

	protected boolean isBigEndian() {
		return getFormat().isBigEndian();
	}

	protected int getChannels() {
		return getFormat().getChannels();
	}

	protected AudioFormat.Encoding getEncoding() {
		return getFormat().getEncoding();
	}

	protected AudioFormat getFormat() {
		return this.audioFormat.updateAndGet(it -> it != null ? it
			: AudioUtils.resolveAudioFormat(getAudio()));
	}

	protected float getFrameRate() {
		return getSampleRate();
	}

	protected int getFrameSize() {

		int channels = getChannels();
		int sampleSizeInBits = getSampleSizeInBits();

		return AudioUtils.isSpecified(sampleSizeInBits) && AudioUtils.isSpecified(channels)
			? (sampleSizeInBits + 7) / BITS_PER_BYTE * channels
			: AudioSystem.NOT_SPECIFIED;
	}

	protected float getSampleRate() {
		return getFormat().getSampleRate();
	}

	protected int getSampleSizeInBits() {

		int sampleSizeInBits = getFormat().getSampleSizeInBits();

		if (AudioUtils.isNotSpecified(sampleSizeInBits)) {

			FFProbe.Format probeFormat = AudioUtils.probeFormat(getAudio());

			int audioSizeInBytes = probeFormat.size();
			int audioSizeInBits = audioSizeInBytes * BITS_PER_BYTE;
			int audioDurationInSeconds = asInt(probeFormat.getDuration().toSeconds());
			int sampleRate = asInt(getFormat().getSampleRate()); // samples per second
			int totalSamples = audioDurationInSeconds * sampleRate;

			sampleSizeInBits = audioSizeInBits / totalSamples;
		}

		return sampleSizeInBits;
	}

	public AudioFormat build() {
		return new AudioFormat(getEncoding(), getSampleRate(), getSampleSizeInBits(), getChannels(),
			getFrameSize(), getFrameRate(), isBigEndian(), getAudioProperties());
	}
}
