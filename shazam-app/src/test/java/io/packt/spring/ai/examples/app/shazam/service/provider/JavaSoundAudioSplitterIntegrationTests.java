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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.DocumentAudioInputStreamSource;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.cp.elements.io.FileSystemUtils;
import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

/**
 * Integration Tests for {@link JavaSoundAudioSplitter}.
 *
 * @author John Blum
 * @see JavaSoundAudioSplitter
 * @see AbstractShazamIntegrationTests
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest(properties = "shazam.audio.clip.duration=1s")
class JavaSoundAudioSplitterIntegrationTests extends AbstractShazamIntegrationTests {

	private static final boolean DEBUG = false;

	// a frame is effectively all the data for a sample in both channels
	// frame rate is the number of frames per second (same as samples per second; 44,100)
	// frame size is sample size (16 bits; 2 bytes) * number of channels per sample (1 = Mono, 2 = Stereo)
	// frame rate | sample rate (44,100) * frame size (4 bytes = sample size (16 bits; 2 bytes) * channels (2 Stereo))
	// bytes per second = frame size (bytes) * frame rate = sample size (bits) / bits per byte * sample rate * channels
	private static final int EXPECTED_AUDIO_CLIP_DATA_SIZE = 44_100 * 4; // frame rate * frame size = 176,400 bytes
	private static final int EXPECTED_AUDIO_CLIP_SIZE = EXPECTED_AUDIO_CLIP_DATA_SIZE + AudioUtils.PCM_WAV_FILE_HEADER_SIZE; // bytes
	private static final int NUMBER_OF_AUDIO_CLIPS = 2;

	private static final String AUDIO_FILENAME_TEMPLATE = "PearlJam-Ten-Jeremy-%d.wav";
	private static final String RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	@Value("${shazam.audio.clip.duration:10s}")
	private Duration audioClipDuration;

	@Autowired
	private JavaSoundAudioSplitter audioSplitter;

	@Test
	@EnabledIf("resourceExists")
	void splitsAudio() {

		Audio audio = Audio.from(resource());
		List<Document> documents = this.audioSplitter.split(audio);

		assertDocuments(audio, documents);
		saveAudioClip(audio, Duration.ofSeconds(5));

		int audioByteOffset = 0;
		int documentCount = 0;
		long audioTimestamp = 0L;
		long documentsAudioSize = AudioUtils.PCM_WAV_FILE_HEADER_SIZE;
		long documentsDataSize = 0;

		for (Document document : documents) {

			byte[] data = resolveData(document);

			if (++documentCount < documents.size()) {
				assertThat(data).hasSize(EXPECTED_AUDIO_CLIP_SIZE);
			}
			else {
				assertThat(data).hasSizeGreaterThan(0).hasSizeLessThanOrEqualTo(EXPECTED_AUDIO_CLIP_SIZE);
			}

			documentsDataSize += data.length;

			if (isNonOverlappingDocument(document)) {
				assertThat(resolveAudioByteOffset(document)).isEqualTo(audioByteOffset);
				assertThat(resolveAudioTimestamp(document)).isEqualTo(audioTimestamp);
				int audioDataSize = data.length - AudioUtils.PCM_WAV_FILE_HEADER_SIZE;
				audioTimestamp += this.audioClipDuration.toMillis();
				audioByteOffset += audioDataSize;
				documentsAudioSize += audioDataSize;
			}
		}

		// The size of non-overlapping Documents (audio clips) in bytes should be equal to the size of the Audio in bytes
		assertThat(documentsAudioSize).isEqualTo(audio.size());

		// The size of all Documents (audio clips) in bytes should be greater than the size of the Audio in bytes
		// given overlap and audio file header size
		assertThat(documentsDataSize).isGreaterThan(audio.size());

		assertAudioClip(audio, documents);
	}

	// expected documents size is x2 for overlap and +1 for overflow
	private void assertAudioClip(Audio audio, List<Document> documents) {

		Document document = selectDocument(documents);

		try (AudioInputStream in = openInputStream(audio, document)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int readLimit = Math.max(resolveData(document).length, in.available());
			in.mark(readLimit);
			AudioSystem.write(in, AudioUtils.WAV_AUDIO_FILE_FORMAT, out);
			byte[] audioClipData = out.toByteArray();
			in.reset();
			assertThat(audioClipData).hasSizeGreaterThanOrEqualTo(resolveData(document).length);
			saveAudioBytes(audioClipData);
			saveAudioStream(in);
		}
		catch (IOException cause) {
			fail("Failed to write audio", cause);
		}
	}

	private void assertDocuments(Audio audio, List<Document> documents) {
		assertThat(documents).isNotNull();
		assertThat(documents).hasSize(asInt(audio.size() / EXPECTED_AUDIO_CLIP_DATA_SIZE) + 1);
	}

	private Document selectDocument(List<Document> documents) {

		List<Document> nonOverlappingDocuments = documents.stream()
			.filter(this::isNonOverlappingDocument)
			.toList();

		int index = NumberUtils.randomInt(nonOverlappingDocuments.size());

		return nonOverlappingDocuments.get(index);
	}

	private AudioInputStream openInputStream(Audio audio, Document document) {
		return DocumentAudioInputStreamSource.builder(audio).using(document).build().get();
	}

	private AudioInputStream openInputStream(AudioInputStream in, long frameLength) {
		return AudioInputStreamBuilder.from(in).withFrameLength(frameLength).build();
	}

	private boolean isNonOverlappingDocument(Document document) {
		Object value = document.getMetadata().get(JavaSoundAudioSplitter.AUDIO_CLIP_OVERLAP_KEY);
		return !Boolean.TRUE.equals(value);
	}

	private int resolveAudioByteOffset(Document document) {
		return (int) document.getMetadata().get(JavaSoundAudioSplitter.AUDIO_BYTE_OFFSET_KEY);
	}

	private long resolveAudioTimestamp(Document document) {
		return (long) document.getMetadata().get(JavaSoundAudioSplitter.AUDIO_TIMESTAMP_KEY);
	}

	@SuppressWarnings("all")
	private byte[] resolveData(Document document) {
		return ExceptionThrowingSupplier.getSafely(() -> document.getMedia().getDataAsByteArray(), cause -> {
			String message = "Failed to get Audio from Document [%s]".formatted(document.getId());
			throw AudioAccessException.because(message, cause);
		});
	}

	@Override
	protected boolean isDebug() {
		return DEBUG;
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	private void saveAudioBytes(byte[] audioData) {

		if (isDebug()) {
			File audioFile = toAudioFile(100);
			try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(audioFile))) {
				out.write(audioData);
				out.flush();
			}
			catch (IOException cause) {
				throw AudioAccessException.because("Failed to save audio data [%d] to file", cause);
			}
		}
	}

	private void saveAudioClip(Audio audio, Duration duration) {

		if (isDebug()) {
			AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

			audio.in(audioFormat);

			int frameRate = Math.round(audioFormat.getFrameRate());

			long durationInSeconds = duration.toSeconds();
			long frameLength = durationInSeconds * frameRate;

			try (AudioInputStream in = AudioInputStreamBuilder.from(audio).build()) {
				for (int index = 0; index < NUMBER_OF_AUDIO_CLIPS; index++) {
					Assert.state(in.available() > 0, "AudioInputStream has no more data");
					try (AudioInputStream audioClipInputStream = openInputStream(in, frameLength)) {
						File audioFile = toAudioFile(index + 1);
						AudioSystem.write(audioClipInputStream, AudioUtils.WAV_AUDIO_FILE_FORMAT, audioFile);
					}
				}
			}
			catch (IOException cause) {
				String message = "Failed to save [%s] of audio to file".formatted(duration);
				throw AudioAccessException.because(message, cause);
			}
		}
	}

	private void saveAudioStream(AudioInputStream in) throws IOException {

		if (isDebug()) {
			File audioFile = toAudioFile(0);
			AudioSystem.write(in, AudioUtils.WAV_AUDIO_FILE_FORMAT, audioFile);
		}
	}

	private File toAudioFile(int fileNumber) {
		String audioFilename = AUDIO_FILENAME_TEMPLATE.formatted(fileNumber);
		return new File(FileSystemUtils.WORKING_DIRECTORY, audioFilename);
	}

	@SpringBootConfiguration
	@EnableConfigurationProperties(AudioProperties.class)
	static class TestConfiguration {

		@Bean
		AudioSplitter audioSplitter(AudioProperties audioProperties) {
			return new JavaSoundAudioSplitter(audioProperties);
		}
	}
}
