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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.ext.ffmpeg.FFProbe;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioFormat} implementation for Shazam used to capture additional metadata, like duration of {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see FFProbe
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
@SuppressWarnings("unused")
@Getter(AccessLevel.PROTECTED)
public class ShazamAudioFormat extends AudioFormat {

	private final AtomicReference<FFProbe.Format> probeFormat = new AtomicReference<>();
	private final AtomicReference<Integer> sampleSizeInBits = new AtomicReference<>();

	private final Audio audio;

	public ShazamAudioFormat(Audio audio, Encoding encoding, int channels, float sampleRate, int sampleSizeInBits,
			float frameRate, int frameSize, boolean bigEndian, Map<String, Object> properties) {

		super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
		this.audio = AudioUtils.assertAudio(audio);
	}

	public long getAudioSize() {
		return getAudio().size();
	}

	public Integer getBitRate() {
		return getProbeFormat().bitRate();
	}

	public Duration getDuration() {
		return getProbeFormat().getDuration();
	}

	public Long getFrameLength() {
		long audioSize = getAudioSize();
		long frameSize = getFrameSize();
		return audioSize / frameSize;
	}

	@Override
	public float getFrameRate() {
		float frameRate = super.getFrameRate();
		return AudioUtils.isSpecified(asInt(frameRate)) ? frameRate
			: getSampleRate();
	}

	@Override
	public int getFrameSize() {

		int frameSize = super.getFrameSize();

		if (AudioUtils.isNotSpecified(frameSize)) {
			int sampleSizeInBits = getSampleSizeInBits();
			if (AudioUtils.isSpecified(sampleSizeInBits)) {
				int sampleSizeInBytes = sampleSizeInBits / BITS_PER_BYTE;
				int numberOfChannels = Math.max(getChannels(), AudioChannels.MONO.value());
				int computedFrameSize = sampleSizeInBytes * numberOfChannels;
				frameSize = Math.max(computedFrameSize, 1);
			}
		}

		return frameSize;
	}

	public FFProbe.Format getProbeFormat() {
		return this.probeFormat.updateAndGet(this::resolveProbeFormat);
	}

	private FFProbe.Format resolveProbeFormat(FFProbe.Format probeFormat) {
		return probeFormat != null ? probeFormat : AudioUtils.probeFormat(getAudio());
	}

	public int getProbeFormatSize() {
		return getProbeFormat().size();
	}

	@Override
	public int getSampleSizeInBits() {
		return this.sampleSizeInBits.updateAndGet(this::resolveSampleSizeInBits);
	}

	private Integer resolveSampleSizeInBits(Integer sampleSizeInBits) {

		if (sampleSizeInBits == null) {

			sampleSizeInBits = super.getSampleSizeInBits();

			if (AudioUtils.isNotSpecified(sampleSizeInBits)) {

				int audioSizeInBytes = getSize();
				int audioSizeInBits = audioSizeInBytes * BITS_PER_BYTE;
				int audioDurationInSeconds = asInt(getDuration().toSeconds());
				int sampleRate = asInt(getSampleRate()); // samples per second
				int totalSamples = audioDurationInSeconds * sampleRate;

				sampleSizeInBits = audioSizeInBits / totalSamples;
			}
		}

		return sampleSizeInBits;
	}

	public Integer getSize() {
		int size = NumberUtils.asInt(getAudioSize());
		return AudioUtils.isSpecified(size) ? size : getProbeFormatSize();
	}
}
