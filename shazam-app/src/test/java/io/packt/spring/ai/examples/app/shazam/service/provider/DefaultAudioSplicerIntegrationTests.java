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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplicer;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.cp.elements.io.FileUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link DefaultAudioSplicer}.
 * <p>
 * This test is experimental.
 *
 * @author John Blum
 * @see Audio
 * @see DefaultAudioSplicer
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@SuppressWarnings("unused")
public class DefaultAudioSplicerIntegrationTests {

	private static final boolean DEBUG = false;

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Autowired
	private AudioSplicer audioSplicer;

	@Autowired
	private AudioSplitter audioSplitter;

	@Test
	@EnabledIf("resourceExists")
	void splicesAudioData() throws IOException {

		Resource audioResource = resource();
		File audioFile = audioResource.getFile();
		Audio audio = Audio.from(audioFile);

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

		assertThat(Arrays.equals(audioData, splicedAudioData)).isTrue();
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}

	private void saveToFile(Audio audio, File sourceAudioFile) {

		if (DEBUG) {
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
	@EnableConfigurationProperties(AudioProperties.class)
	static class TestConfiguration {

		@Bean
		AudioSplicer audioSplicer() {
			return new DefaultAudioSplicer();
		}

		@Bean
		AudioSplitter audioSplitter(AudioProperties audioProperties) {
			return new JavaSoundAudioSplitter(audioProperties);
		}
	}
}
