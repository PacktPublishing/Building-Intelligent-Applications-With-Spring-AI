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

import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for Java Sound {@link AudioFileFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFileFormat
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
class AudioFileFormatIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Test
	@EnabledIf("resourceExists")
	void audioFileFormat() throws UnsupportedAudioFileException, IOException {

		Audio audio = Audio.from(resource());
		AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(audio.file());

		assertThat(audioFileFormat).isNotNull();
		assertThat(audioFileFormat.getByteLength()).isEqualTo(audio.getData().length);
		assertThat(audioFileFormat.getFrameLength()).isEqualTo(AudioSystem.NOT_SPECIFIED);
		assertThat(audioFileFormat.getType().getExtension()).isEqualTo("mpeg");
		assertThat(audioFileFormat.properties()).isNull();
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}
}
