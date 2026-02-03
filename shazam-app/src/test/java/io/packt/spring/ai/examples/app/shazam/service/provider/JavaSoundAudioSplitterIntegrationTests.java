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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioChannels;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.cp.elements.io.FileSystemUtils;
import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
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
@SuppressWarnings("unused")
class JavaSoundAudioSplitterIntegrationTests extends AbstractShazamIntegrationTests {

	private static final boolean DEBUG = true;

	private static final int EXPECTED_AUDIO_CLIP_SIZE = 176_400; // bytes
	private static final int NUMBER_OF_AUDIO_CLIPS = 2;

	private static final String AUDIO_FILENAME_TEMPLATE = "PearlJam-Ten-Jeremy-%d.wav";
	private static final String RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	@Autowired
	private JavaSoundAudioSplitter audioSplitter;

	/*
	 * Matchbox20-Unwell.mp3 METADATA
	 *
	 * 3:49 minutes & seconds of audio / 229 seconds of audio
	 * ~2.3 MB of audio / 2,287,671 bytes of audio / 18,301,368 bits of audio
	 * 22.05 kHz (22,050 Hz) == samples per second
	 * 22.05 kHz * 229 seconds == 5,049,450 samples
	 * How many bits per sample? (MP3 compression skews the calculation)
	 *
	 * 18,301,368 bits / 229 seconds == ~79,919 bits per second (bit rate)
	 * ~79,919 bits per second / 22,050 samples per second ==  ~3.62 bits / sample
	 *
	 * 18,301,368 bits / 320,000 bps = ~57 seconds
	 * 18,301,368 bits / 128,000 bps = ~143 seconds
	 * 18,301,368 bits / 64,000 bps = ~286 seconds
	 * 18,301,368 bits / (128k + 64k = 192k / 2 = 96 kbps) = ~191 seconds
	 */
	@Test
	@EnabledIf("resourceExists")
	void splitsAudio() {

		Audio audio = Audio.from(resource());
		List<Document> documents = this.audioSplitter.split(audio);

		saveAudioClip(audio, Duration.ofSeconds(10));

		assertThat(documents).isNotNull();
		assertThat(documents).hasSize(expectedNumberOfDocuments(audio));

		long documentsAudioSize = AudioUtils.PCM_WAV_FILE_HEADER_SIZE;
		long documentsDataSize = 0;

		for (Document document : documents) {
			byte[] data = resolveData(document);
			assertThat(data).hasSizeGreaterThan(0).hasSizeLessThanOrEqualTo(EXPECTED_AUDIO_CLIP_SIZE);
			documentsAudioSize += isNonOverlappingDocument(document) ? data.length : 0;
			documentsDataSize += data.length;
		}

		// The size of the Documents (audio clips) in bytes should be greater than
		// the size of the Audio in bytes given the overlap
		assertThat(documentsAudioSize).isEqualTo(audio.size());
		assertThat(documentsDataSize).isGreaterThan(audio.size());

		assertAudioClip(audio, documents);
	}

	private void assertAudioClip(Audio audio, List<Document> documents) {

		Document document = selectDocument(documents);

		try (AudioInputStream in = openInputStream(audio, document)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			AudioSystem.write(in, AudioUtils.WAV_AUDIO_FILE_FORMAT, out);
			byte[] audioClipData = out.toByteArray();
			assertThat(audioClipData).hasSizeGreaterThanOrEqualTo(resolveData(document).length);
			saveToFile(in);
		}
		catch (IOException cause) {
			fail("Failed to write audio", cause);
		}
	}

	private static int expectedNumberOfDocuments(Audio audio) {
		// divisor == frame rate * frame size + 1
		// frame rate is the number of frames per second
		// frame size is sample size (16 bits) * number of channels per sample
		// a frame is effectively all the data for a logical sample in both channels
		int documentSize = EXPECTED_AUDIO_CLIP_SIZE * AudioChannels.STEREO.value() + 1;
		return asInt(audio.size() / documentSize);
	}

	private Document selectDocument(List<Document> documents) {

		List<Document> nonOverlappingDocuments = documents.stream()
			.filter(this::isNonOverlappingDocument)
			.toList();

		int index = NumberUtils.randomInt(nonOverlappingDocuments.size());

		return nonOverlappingDocuments.get(index);
	}

	private boolean isNonOverlappingDocument(Document document) {
		Object value = document.getMetadata().get(JavaSoundAudioSplitter.AUDIO_CLIP_OVERLAP_KEY);
		return !Boolean.TRUE.equals(value);
	}

	private AudioInputStream openInputStream(Audio audio, Document document) {

		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();
		Audio audioClip = resolveAudio(document).in(audioFormat);
		long frameLength = resolveFrameLength(audioClip);

		return AudioInputStreamBuilder.from(audioClip)
			.withFrameLength(frameLength)
			.build();
	}

	private Audio resolveAudio(Document document) {
		return document instanceof AbstractDocumentStore.AudioDocument audioDocument ? audioDocument.getAudio()
			: Audio.from(resolveData(document));
	}

	@SuppressWarnings("all")
	private byte[] resolveData(Document document) {
		return ExceptionThrowingSupplier.getSafely(document.getMedia()::getDataAsByteArray, cause -> {
			String message = "Failed to get Audio from Document [%s]".formatted(document.getId());
			throw AudioAccessException.because(message, cause);
		});
	}

	private long resolveFrameLength(Audio audio) {
		return audio.getFormat() instanceof ShazamAudioFormat shazamAudioFormat ? shazamAudioFormat.getFrameLength()
			: AudioUtils.calculateFrameLength(audio);
	}

	@Override
	protected boolean isDebug() {
		return DEBUG;
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	private void saveAudioClip(Audio audio, Duration duration) {

		if (isDebug()) {
			AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

			audio.in(audioFormat);

			int frameRate = Math.round(audioFormat.getFrameRate());
			int frameSize = audioFormat.getFrameSize();
			int bytesPerSecond = frameRate * frameSize;

			long durationInSeconds = duration.toSeconds();
			long frameLength = durationInSeconds * frameRate;

			try (AudioInputStream in = AudioInputStreamBuilder.from(audio).build()) {
				for (int index = 0; index < NUMBER_OF_AUDIO_CLIPS; index++) {
					Assert.state(in.available() > 0, "Audio InputStream has no more data");
					log("AUDIO AVAILABLE [%d]%n", in.available());
					try (AudioInputStream audioClipInputStream = AudioInputStreamBuilder.from(in).withFrameLength(frameLength).build()) {
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

	private void saveToFile(AudioInputStream in) throws IOException {

		if (isDebug()) {
			in.reset();
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
