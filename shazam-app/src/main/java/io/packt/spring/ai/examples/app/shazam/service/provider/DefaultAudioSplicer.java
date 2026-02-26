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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplicer;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplicer;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;

import org.cp.elements.lang.Assert;
import org.cp.elements.util.CollectionUtils;
import org.springframework.ai.document.Document;

/**
 * Default implementation of the {@link AudioSplicer}
 *
 * @author John Blum
 * @see Audio
 * @see AbstractAudioSplicer
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
public class DefaultAudioSplicer extends AbstractAudioSplicer {

	@Override
	@SuppressWarnings("all")
	public Audio splice(List<Document> audioDocuments) {

		List<Audio> audioClips = extractAudioClips(audioDocuments);
		List<AudioInputStream> audioInputStreams = new ArrayList<>(audioClips.size());

		AudioFormat audioFormat = null;

		for (Audio audioClip : audioClips) {
			AudioInputStream in = AudioUtils.openInputStream(audioClip);
			AudioFormat format = in.getFormat();
			audioFormat = validateAudioFormat(format, audioFormat);
			audioInputStreams.add(in);
		}

		SequenceInputStream inputStream = newSequenceInputStream(audioInputStreams);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try (AudioInputStream audioInputStream = newAudioInputStream(audioClips, inputStream, audioFormat)) {
			AudioSystem.write(audioInputStream, audioFileFormatType(), outputStream);
			byte[] audioData = outputStream.toByteArray();
			return Audio.from(audioData).in(audioFormat);
		}
		catch (IOException cause) {
			String message = "Failed to splice audio clips in [%d] Documents".formatted(audioDocuments.size());
			throw AudioAccessException.because(message, cause);
		}
	}

	private AudioFormat assertAudioFormat(AudioFormat actual, AudioFormat expected) {
		Assert.isTrue(actual.matches(expected), () -> "AudioFormat [%s] for audio clip did not match expected [%s]"
			.formatted(actual, expected));
		return expected;
	}

	private AudioFormat validateAudioFormat(AudioFormat actual, AudioFormat expected) {
		Assert.notNull(actual, "AudioFormat of audio clip is required");
		return expected != null ? assertAudioFormat(actual, expected) : actual;
	}

	@Override
	protected Predicate<Document> audioClipFilter() {
		return super.audioClipFilter().and(this::isNonOverlappingAudioClip);
	}

	protected AudioFileFormat.Type audioFileFormatType() {
		return AudioFileFormat.Type.WAVE;
	}

	private int computeAudioSize(List<Audio> audioClips) {

		return audioClips.stream()
			.map(Audio::size)
			.reduce(Long::sum)
			.map(Long::intValue)
			.orElse(0);
	}

	private int computeAudioSize(List<Audio> audioClips, InputStream in) throws IOException {
		int audioSize = computeAudioSize(audioClips);
		int inputSize = in.available();
		return Math.max(audioSize, inputSize);
	}

	private int computeFrameLength(List<Audio> audioClips, InputStream in, AudioFormat audioFormat) {

		return ExceptionThrowingSupplier.<Integer>getSafely(() -> {
			int audioSize = computeAudioSize(audioClips, in);
			int frameSize = audioFormat.getFrameSize();
			return audioSize / frameSize;
		}, cause -> AudioSystem.NOT_SPECIFIED);
	}

	private boolean isNonOverlappingAudioClip(Document document) {
		return Boolean.FALSE.equals(document.getMetadata().get(AbstractAudioSplitter.AUDIO_CLIP_OVERLAP_KEY));
	}

	private AudioInputStream newAudioInputStream(List<Audio> audioClips,
			InputStream inputStream, AudioFormat audioFormat) {

		return new AudioInputStream(inputStream, audioFormat, computeFrameLength(audioClips, inputStream, audioFormat));
	}

	private SequenceInputStream newSequenceInputStream(List<AudioInputStream> audioInputStreams) {
		return new SequenceInputStream(CollectionUtils.asEnumeration(audioInputStreams.iterator()));
	}
}
