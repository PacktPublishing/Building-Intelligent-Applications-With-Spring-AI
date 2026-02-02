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
package io.packt.spring.ai.examples.app.shazam.config;

import java.time.Duration;

import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Spring Boot {@link ConfigurationProperties} for {@literal shazam.audio} configuration.
 *
 * @author John Blum
 * @see ConfigurationProperties
 * @since 0.1.0
 */
@Data
@ConfigurationProperties(prefix = "shazam.audio")
public class AudioProperties {

	// 192 kbps / 8 bits per byte * 5 seconds
	public static final int DEFAULT_AUDIO_BUFFER_SIZE = 120_000;
	public static final int DEFAULT_AUDIO_BUFFER_DIVISOR = 20;
	public static final int DEFAULT_MP3_BIT_RATE = 128_000;
	public static final int DEFAULT_MP3_SAMPLE_RATE = 22_050;

	public static final Duration DEFAULT_AUDIO_CLIP_LENGTH = Duration.ofSeconds(5);

	public static AudioProperties defaultAudioProperties() {
		return new AudioProperties();
	}

	public static AudioProperties nullSafe(AudioProperties audioProperties) {
		return audioProperties != null ? audioProperties : defaultAudioProperties();
	}

	private AudioBuffer buffer = new AudioBuffer();

	private AudioClip clip = new AudioClip();

	private Mp3 mp3 = new Mp3();

	public int getBufferDivisor() {
		return getBufferDivisor(DEFAULT_AUDIO_BUFFER_DIVISOR);
	}

	public int getBufferDivisor(int defaultAudioBufferDivisor) {
		Integer configuredAudioBufferDivisor = getBuffer().getDivisor();
		return configuredAudioBufferDivisor != null ? configuredAudioBufferDivisor : defaultAudioBufferDivisor;
	}

	public int getBufferSize() {
		return getBufferSize(DEFAULT_AUDIO_BUFFER_SIZE);
	}

	public int getBufferSize(int defaultAudioBufferSize) {
		Integer configuredByteBufferSize = getBuffer().getSize();
		return configuredByteBufferSize != null ? configuredByteBufferSize : defaultAudioBufferSize;
	}

	public Duration getClipDuration() {
		return getClipDuration(DEFAULT_AUDIO_CLIP_LENGTH);
	}

	public Duration getClipDuration(Duration defaultAudioClipLength) {
		Duration configuredAudioClipLength = getClip().getDuration();
		return TimeUtils.isNotZero(configuredAudioClipLength) ? configuredAudioClipLength : defaultAudioClipLength;
	}

	public int getMp3BitRate() {
		return getMp3BitRate(DEFAULT_MP3_BIT_RATE);
	}

	public int getMp3BitRate(int defaultMp3BitRate) {
		Integer configuredMp3BitRate = getMp3().getBitRate();
		return configuredMp3BitRate != null ? configuredMp3BitRate : defaultMp3BitRate;
	}

	public int getMp3SampleRate() {
		return getMp3SampleRate(DEFAULT_MP3_SAMPLE_RATE);
	}

	public int getMp3SampleRate(int defaultMp3SampleRate) {
		Integer configuredMp3SampleRate = getMp3().getSampleRate();
		return configuredMp3SampleRate != null ? configuredMp3SampleRate : defaultMp3SampleRate;
	}

	@Data
	public static class AudioBuffer {
		private Integer divisor;
		private Integer size;
	}

	@Data
	public static class AudioClip {
		private Duration duration;
	}

	@Data
	public static class Mp3 {
		private Integer bitRate;
		private Integer sampleRate;
	}
}
