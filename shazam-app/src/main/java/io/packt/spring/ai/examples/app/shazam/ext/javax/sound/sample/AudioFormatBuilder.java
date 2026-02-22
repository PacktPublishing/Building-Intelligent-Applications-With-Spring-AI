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

import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.BITS_PER_BYTE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.MapUtils;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Builder for Java Sound {@link AudioFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatResolver
 * @see ShazamAudioFormat
 * @see javax.sound.sampled.AudioFormat
 * @see org.cp.elements.lang.Builder
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class AudioFormatBuilder implements Builder<AudioFormat> {

	public static AudioFormatBuilder from(Audio audio) {
		return new AudioFormatBuilder(audio);
	}

	private final AtomicReference<AudioFormat> audioFormat = new AtomicReference<>();

	private final Audio audio;

	private AudioFormat.Encoding encoding;

	private Boolean bigEndian;

	private Float frameRate;
	private Float sampleRate;

	private Integer channels;
	private Integer frameSize;
	private Integer sampleSizeInBits;

	private final Map<String, Object> audioProperties = new HashMap<>();

	private volatile Supplier<AudioFormat> defaultAudioFormat;

	protected AudioFormatBuilder(Audio audio) {
		this.audio = AudioUtils.assertAudio(audio);
	}

	public AudioFormatBuilder copy(AudioFormat audioFormat) {
		AudioUtils.assertAudioFormat(audioFormat);
		this.audioFormat.set(audioFormat);
		this.audioProperties.putAll(audioFormat.properties());
		return this;
	}

	public AudioFormatBuilder copyAudioFormat(AudioInputStream audioInputStream) {
		AudioUtils.assertAudioInputStream(audioInputStream);
		return copy(audioInputStream.getFormat());
	}

	public AudioFormatBuilder defaultAudioFormat(AudioFormat defaultAudioFormat) {
		return defaultAudioFormat(() -> defaultAudioFormat);
	}

	public AudioFormatBuilder defaultAudioFormat(Supplier<AudioFormat> defaultAudioFormat) {
		this.defaultAudioFormat = defaultAudioFormat;
		return this;
	}

	protected @Nullable AudioFormat getAudioFormat() {
		return ConfiguredAudioFormatResolver.INSTANCE.resolve(getAudio());
	}

	protected boolean isBigEndian() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.bigEndian, () -> getFormat().isBigEndian());
	}

	protected int getChannels() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.channels, () -> getFormat().getChannels());
	}

	protected AudioFormat.Encoding getEncoding() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.encoding, () -> getFormat().getEncoding());
	}

	protected AudioFormat getFormat() {
		return this.audioFormat.updateAndGet(this::resolveFormat);
	}

	private AudioFormat resolveFormat(AudioFormat audioFormat) {
		return audioFormat != null ? audioFormat : resolveFormat(getAudio());
	}

	private AudioFormat resolveFormat(Audio audio) {
		return AudioFormatResolver.defaultAudioFormatResolver().resolve(audio, getDefaultAudioFormat());
	}

	protected float getFrameRate() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.frameRate, this::getSampleRate);
	}

	protected int getFrameSize() {

		return ObjectUtils.returnValueOrDefaultIfNull(this.frameSize, () -> {

			int channels = Math.max(getChannels(), AudioChannels.MONO.value());
			int sampleSizeInBits = getSampleSizeInBits();

			return AudioUtils.isSpecified(sampleSizeInBits) && AudioUtils.isSpecified(channels)
				? (sampleSizeInBits + 7) / BITS_PER_BYTE * channels
				: AudioUtils.unspecified();
		});
	}

	protected float getSampleRate() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.sampleRate, () -> getFormat().getSampleRate());
	}

	protected int getSampleSizeInBits() {
		return ObjectUtils.returnValueOrDefaultIfNull(this.sampleSizeInBits, () -> getFormat().getSampleSizeInBits());
	}

	public AudioFormatBuilder withBigEndian(Boolean bigEndian) {
		this.bigEndian = bigEndian;
		return this;
	}

	public AudioFormatBuilder withChannels(Integer channels) {
		this.channels = channels;
		return this;
	}

	public AudioFormatBuilder withFrameRate(Float frameRate) {
		this.frameRate = frameRate;
		return this;
	}

	public AudioFormatBuilder withFrameSize(Integer frameSize) {
		this.frameSize = frameSize;
		return this;
	}

	public AudioFormatBuilder withProperty(String propertyName, Object propertyValue) {
		Assert.hasText(propertyName, "Property name is required");
		Assert.notNull(propertyValue, "Property value is required");
		this.audioProperties.put(propertyName, propertyValue);
		return this;
	}

	public AudioFormatBuilder withProperties(Map<String, Object> properties) {

		MapUtils.nullSafeMap(properties).entrySet().stream()
			.filter(entry -> StringUtils.hasText(entry.getKey()) && Objects.nonNull(entry.getValue()))
			.forEach(entry -> withProperty(entry.getKey(), entry.getValue()));

		return this;
	}

	public AudioFormatBuilder withSampleRate(Float sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}

	public AudioFormatBuilder withSampleSizeInBits(Integer frameSize) {
		this.frameSize = frameSize;
		return this;
	}

	public AudioFormat build() {

		AudioFormat audioFormat = getAudioFormat();

		return audioFormat instanceof ShazamAudioFormat shazamAudioFormat ? shazamAudioFormat
			: newAudioFormat();
	}

	private ShazamAudioFormat newAudioFormat() {
		return new ShazamAudioFormat(getAudio(), getEncoding(), getChannels(), getSampleRate(), getSampleSizeInBits(),
			getFrameRate(), getFrameSize(), isBigEndian(), getAudioProperties());
	}
}
