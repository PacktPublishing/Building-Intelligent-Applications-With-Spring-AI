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
import static org.assertj.core.api.Assumptions.assumeThat;

import java.time.Duration;
import java.util.List;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link JavaSoundAudioSplitter}.
 *
 * @author John Blum
 * @see JavaSoundAudioSplitter
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest(properties = "shazam.audio.clip.length=5s")
@SuppressWarnings("unused")
class JavaSoundAudioSplitterIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

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
	void readsAndSplitsMp3() {

		Resource mp3 = new ClassPathResource(RESOURCE_PATH);

		assumeThat(mp3.exists())
			.describedAs("MP3 [%s] is not present", mp3)
			.isTrue();

		Audio audio = Audio.from(mp3).havingDuration(Duration.ofMinutes(3).plusSeconds(49));

		List<Document> documents = this.audioSplitter.split(audio);

		assertThat(documents).isNotNull();
		assertThat(documents).hasSize(91);

		long documentsSize = 0;

		for (Document document : documents) {
			Media media = document.getMedia();
			byte[] documentData = media.getDataAsByteArray();
			documentsSize += documentData.length;
		}

		// The size of the Documents in bytes should be greater than the Audio size in bytes given the overlap
		assertThat(documentsSize).isGreaterThan(audio.getData().length);
	}

	boolean resourceExists() {
		return new ClassPathResource(RESOURCE_PATH).exists();
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
