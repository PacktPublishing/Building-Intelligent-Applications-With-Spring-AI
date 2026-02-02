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
package io.packt.spring.ai.examples.app.shazam.service.provider;

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioSplitter} implementation that divides the sound bytes of the {@link Audio} into equal chunks.
 *
 * @author John Blum
 * @see Audio
 * @see AudioProperties
 * @see AbstractAudioSplitter
 * @see org.springframework.ai.document.Document
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@Profile("AudioSpace")
@SuppressWarnings("unused")
@Getter(AccessLevel.PROTECTED)
public class SoundBytesAudioSplitter extends AbstractAudioSplitter {

	private final SoundBytesCalculator calculator;

	public SoundBytesAudioSplitter(AudioProperties audioProperties, SoundBytesCalculator calculator) {
		super(audioProperties);
		this.calculator = assertSoundBytesCalculator(calculator);
	}

	private SoundBytesCalculator assertSoundBytesCalculator(SoundBytesCalculator calculator) {
		Assert.notNull(calculator, "SoundBytesCalculator is required");
		return calculator;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		getLogger().info("Using [{}]", calculator);
	}

	@Override
	public List<Document> split(Audio audio) {

		byte[] audioData = audio.getData();
		int audioBufferSize = getCalculator().calculate(audio);

		AudioClip previousAudioClip = null;

		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		List<Document> documents = new ArrayList<>();

		for (int index = 0; index < audioData.length; index += audioBufferSize) {
			int length = Math.min(audioData.length - index, audioBufferSize);
			byte[] audioBuffer = newAudioBuffer(audioData, index, length);
			AudioClip audioClip = AudioClip.from(audioBuffer, audioFormat);
			Document document = buildDocument(audioClip, false);
			documents.add(document);

			if (previousAudioClip != null) {
				AudioClip overlappingAudioClip = previousAudioClip.secondHalf().merge(audioClip.firstHalf());
				Document overlappingDocument = buildDocument(overlappingAudioClip, true);
				documents.add(overlappingDocument);
			}

			previousAudioClip = audioClip;
		}

		return documents;
	}

	private byte[] newAudioBuffer(byte[] audioData, int position, int length) {
		byte[] audioBuffer = new byte[length];
		System.arraycopy(audioData, position, audioBuffer, 0, length);
		return audioBuffer;
	}

	public interface SoundBytesCalculator {
		int calculate(Audio audio);
	}

	@Getter(AccessLevel.PROTECTED)
	protected static abstract class AbstractSoundBytesCalculator implements SoundBytesCalculator {

		private final AudioProperties audioProperties;

		protected AbstractSoundBytesCalculator(AudioProperties audioProperties) {
			Assert.notNull(audioProperties, "AudioProperties are required");
			this.audioProperties = audioProperties;
		}

		@SuppressWarnings("all")
		protected int calculateFromBitRate(Audio audio, int bitRate) {

			Duration audioClipLength = getAudioProperties().getClipDuration();

			int audioClipLengthInSeconds = asInt(audioClipLength.toSeconds());
			int bitsPerAudioClipLengthInSeconds = bitRate * audioClipLengthInSeconds;
			int bytesPerAudioClipLengthInSeconds = bitsPerAudioClipLengthInSeconds / NumberUtils.BITS_PER_BYTE;

			return bytesPerAudioClipLengthInSeconds;
		}
	}

	@Service
	@Profile("CD")
	public static class CompactDiscSoundBytesCalculator extends AbstractSoundBytesCalculator implements CompactDiscMetadata {

		protected static final int CD_CHANNELS = 2; // Stereo
		protected static final int CD_BIT_RATE = CD_SAMPLE_RATE * CD_SAMPLE_SIZE_IN_BITS * CD_CHANNELS;

		public CompactDiscSoundBytesCalculator(AudioProperties audioProperties) {
			super(audioProperties);
		}

		@Override
		@SuppressWarnings("all")
		public int calculate(Audio audio) {
			return calculateFromBitRate(audio, CD_BIT_RATE);
		}

		@Override
		public String toString() {
			return "SoundBytesCalculator for Compact Disc (CD)";
		}
	}

	@Service
	@Profile("MP3")
	public static class MpegSoundBytesCalculator extends AbstractSoundBytesCalculator implements MpegMetadata {

		public MpegSoundBytesCalculator(AudioProperties audioProperties) {
			super(audioProperties);
		}

		@Override
		public int calculate(Audio audio) {
			int mp3BitRate = getAudioProperties().getMp3BitRate(MP3_BIT_RATE);
			return calculateFromBitRate(audio, mp3BitRate);
		}

		@Override
		public String toString() {
			return "SoundBytesCalculator for MP3";
		}
	}

	@Service
	@Profile({ "!CD", "!MP3" })
	public static class SpaceSoundBytesCalculator extends AbstractSoundBytesCalculator {

		public SpaceSoundBytesCalculator(AudioProperties audioProperties) {
			super(audioProperties);
		}

		@Override
		public int calculate(Audio audio) {

			int audioSizeInBytes = audio.getData().length;
			int configuredBufferDivisor = getAudioProperties().getBufferDivisor();
			int configuredBufferSize = getAudioProperties().getBufferSize();
			int calculatedBufferSize = audioSizeInBytes / configuredBufferDivisor;

			return Math.max(calculatedBufferSize, configuredBufferSize);
		}

		@Override
		public String toString() {
			return "SoundByteCalculator by Space";
		}
	}
}
