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
package io.packt.spring.ai.examples.app.shazam.ext.javax.sound;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link AudioFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see org.junit.jupiter.api.Test
 * @see javax.sound.sampled.AudioFormat
 * @since 1.0.0
 */
@SuppressWarnings("unused")
class AudioFormatIntegrationTests {

	private static final String MATCHBOX20_UNWELL_RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	// Throws UnsupportedAudioFileException: Stream of unsupported format
	private static final String PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH = "PearlJam-NoCode-RedMosquito.mp3";

	private static final String RESOURCE_PATH = MATCHBOX20_UNWELL_RESOURCE_PATH;

	@Test
	void audioFormat() {

		Audio audio = Audio.from(resource());
		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		assertThat(audioFormat).isNotNull();
		assertThat(audioFormat.getChannels()).isEqualTo(2);
		assertThat(audioFormat.getEncoding()).isEqualTo(new AudioFormat.Encoding("MPEG2L3"));
		assertThat(audioFormat.getFrameRate()).isEqualTo(22_050f);
		assertThat(audioFormat.getFrameSize()).isEqualTo(AudioSystem.NOT_SPECIFIED);
		assertThat(audioFormat.getSampleRate()).isEqualTo(22_050f);
		assertThat(audioFormat.getSampleSizeInBits()).isEqualTo(3);
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}
}
