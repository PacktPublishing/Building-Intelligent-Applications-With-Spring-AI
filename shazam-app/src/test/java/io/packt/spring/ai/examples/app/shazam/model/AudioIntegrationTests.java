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
package io.packt.spring.ai.examples.app.shazam.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class AudioIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Test
	@EnabledIf("resourceExists")
	void encodeAndDecodeAudioBytesInBase64() {

		Resource matchbox20unwell = new ClassPathResource(RESOURCE_PATH);
		Audio audio = Audio.from(matchbox20unwell);

		assertThat(audio).isNotNull();

		byte[] audioData = audio.getData();

		assertThat(audioData).isNotEmpty();
		assertThat(audioData).hasSizeGreaterThan(2_000_000); // 2 MB

		String audioBase64 = audio.encode();

		assertThat(audioBase64).isNotEmpty();

		Audio decodedAudio = Audio.decode(audioBase64);

		assertThat(decodedAudio).isNotNull();
		assertThat(decodedAudio).isNotSameAs(audio);
		assertThat(decodedAudio.getData()).isEqualTo(audioData);
	}

	boolean resourceExists() {
		return new ClassPathResource(RESOURCE_PATH).exists();
	}
}
