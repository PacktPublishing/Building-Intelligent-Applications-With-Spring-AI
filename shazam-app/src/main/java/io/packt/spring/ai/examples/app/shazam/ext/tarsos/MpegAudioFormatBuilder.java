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
package io.packt.spring.ai.examples.app.shazam.ext.tarsos;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.tritonus.sampled.file.mpeg.MpegAudioFileWriter;

import lombok.Getter;

/**
 * {@link AudioFormat} {@link Builder} used to build an {@literal MPEG} {@link AudioFormat}
 * compatible with the {@literal Tarsos} library.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFormat
 * @see javax.sound.sampled.AudioSystem
 * @see org.cp.elements.lang.Builder
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class MpegAudioFormatBuilder implements Builder<AudioFormat> {

	protected static final AudioFormat.Encoding MPEG_ONE_LAYER_THREE_ENCODING = MpegAudioFileWriter.MPEG1L3;

	public static MpegAudioFormatBuilder mpegOneLayerThree(Audio audio) {
		return new MpegAudioFormatBuilder(audio, MPEG_ONE_LAYER_THREE_ENCODING);
	}

	private final Audio audio;

	private final AudioFormat.Encoding encoding;

	private boolean bigEndian = true;

	private float frameRate = AudioSystem.NOT_SPECIFIED;
	private float sampleRate = AudioSystem.NOT_SPECIFIED;

	private int channels = 2; // STEREO
	private int frameSize = AudioSystem.NOT_SPECIFIED;
	private int sampleSize = AudioSystem.NOT_SPECIFIED;

	private final Map<String, Object> properties = new HashMap<>();

	protected MpegAudioFormatBuilder(Audio audio, AudioFormat.Encoding encoding) {
		Assert.notNull(encoding, "AudioFormat Encoding is required");
		this.audio = AudioUtils.assertAudio(audio);
		this.encoding = encoding;
	}

	public MpegAudioFormatBuilder inBigEndian() {
		this.bigEndian = true;
		return this;
	}

	public MpegAudioFormatBuilder inLittleEndian() {
		this.bigEndian = false;
		return this;
	}

	public MpegAudioFormatBuilder inMono() {
		this.channels = 1;
		return this;
	}

	public MpegAudioFormatBuilder inStereo() {
		this.channels = 2;
		return this;
	}

	public MpegAudioFormatBuilder withFrameRate(float frameRate) {
		this.frameRate = frameRate;
		return this;
	}

	public MpegAudioFormatBuilder withFrameSize(int frameSize) {
		this.frameSize = frameSize;
		return this;
	}

	public MpegAudioFormatBuilder withSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}

	public MpegAudioFormatBuilder withSampleRateOf22050() {
		return withSampleRate(22_050.0f);
	}

	public MpegAudioFormatBuilder withSampleRateOf44100() {
		return withSampleRate(44_100.0f);
	}

	public MpegAudioFormatBuilder withSameSize(int sampleSize) {
		this.sampleSize = sampleSize;
		return this;
	}

	@Override
	public AudioFormat build() {

		AudioFormat mpegAudioFormat = buildAudioFormat();

		return AudioFormatBuilder.from(getAudio())
			.copy(mpegAudioFormat)
			.build();
	}

	private AudioFormat buildAudioFormat() {
		return new AudioFormat(getEncoding(), getSampleRate(), getSampleSize(), getChannels(),
			getFrameSize(), getFrameRate(), isBigEndian(), getProperties());
	}
}
