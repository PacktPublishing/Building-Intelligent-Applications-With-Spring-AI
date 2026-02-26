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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.config.ShazamConfiguration;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplicer;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.util.NumberUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.cp.elements.io.FileUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link DefaultAudioSplicer}.
 * <p>
 * This test is experimental.
 *
 * @author John Blum
 * @see Audio
 * @see DefaultAudioSplicer
 * @see AbstractShazamIntegrationTests
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @see SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@SuppressWarnings("unused")
public class DefaultAudioSplicerIntegrationTests extends AbstractShazamIntegrationTests {

	//private static final String RESOURCE_PATH = "PearlJam-NoCode-RedMosquito.mp3";
	private static final String RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	@Autowired
	private AudioSplicer audioSplicer;

	@Autowired
	private AudioSplitter audioSplitter;

	@Test
	@EnabledIf("resourceExists")
	void splicesAudioData() throws IOException {

		Resource audioResource = resource();
		File audioFile = audioResource.getFile();
		Audio audio = Audio.from(audioResource);

		assertThat(audio).isNotNull();
		assertThat(audio.size()).isEqualTo(audioFile.length());

		List<Document> audioClips = this.audioSplitter.split(audio);

		assertThat(audioClips).isNotNull();
		assertThat(audioClips).hasSizeGreaterThan(1);

		Audio splicedAudio = this.audioSplicer.splice(audioClips);

		assertThat(splicedAudio).isNotNull();
		assertThat(splicedAudio).isNotSameAs(audio);
		saveToFile(splicedAudio, audioFile);
		assertAudioDataIsEqual(audio, splicedAudio);
	}

	private void assertAudioDataIsEqual(Audio audio, Audio splicedAudio) {

		byte[] audioData = audio.getData();
		byte[] splicedAudioData = splicedAudio.getData();

		assertThat(splicedAudioData)
			.describedAs("Expected spliced audio data size to be equals to [%d]; but was [%d]",
				audioData.length, NumberUtils.nullSafeLength(splicedAudioData))
			.isNotNull()
			.hasSameSizeAs(audioData);

		// Audio Data (WAVE) Header is different!
		//assertThat(Arrays.equals(audioData, splicedAudioData)).isTrue();

		for (int index = AudioUtils.PCM_WAV_FILE_HEADER_SIZE; index < audioData.length; index++) {
			assertThat(splicedAudioData[index])
				.describedAs("Byte at index [%d] in spliced audio data [%d] is not same as byte in original audio data [%d]",
					index, splicedAudioData[index], audioData[index])
				.isEqualTo(audioData[index]);
		}
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	private void saveToFile(Audio audio, File sourceAudioFile) {

		if (isDebug()) {
			try {
				String audioFilename = FileUtils.getName(sourceAudioFile);
				String audioFileExtension = FileUtils.getExtension(sourceAudioFile);
				String splicedAudioFilename = "%s-spliced.%s".formatted(audioFilename, audioFileExtension);

				File splicedAudioFile = new File(sourceAudioFile.getParentFile(), splicedAudioFilename);

				byte[] audioData = audio.getData();

				try (FileOutputStream fileOut = new FileOutputStream(splicedAudioFile, false)) {
					fileOut.write(audioData);
					fileOut.flush();
				}
			}
			catch (IOException cause) {
				String message = "Failed to save audio [%s] to file".formatted(sourceAudioFile.getAbsolutePath());
				throw new IllegalStateException(message, cause);
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ShazamConfiguration.class)
	static class TestConfiguration {

		@Bean
		AudioFingerprintFunction<?> mockAudioFingerprintFunction() {
			return mock(AudioFingerprintFunction.class);
		}
	}
}
