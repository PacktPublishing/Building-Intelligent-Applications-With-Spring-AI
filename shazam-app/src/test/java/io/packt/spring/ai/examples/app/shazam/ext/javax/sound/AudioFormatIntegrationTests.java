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

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link AudioFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatBuilder
 * @see org.junit.jupiter.api.Test
 * @see javax.sound.sampled.AudioFormat
 * @since 1.0.0
 */
@SuppressWarnings("unused")
class AudioFormatIntegrationTests {

	private static final boolean DEBUG = false;

	private static final String MATCHBOX20_UNWELL_RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	// Throws UnsupportedAudioFileException: Stream of unsupported format - MP3 ripped from CD
	private static final String PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH = "PearlJam-NoCode-RedMosquito.mp3";

	private static final String PEARL_JAM_TEN_OCEANS = "PearlJam-Ten-Oceans.wav";

	private static final String RESOURCE_PATH = MATCHBOX20_UNWELL_RESOURCE_PATH;
	//private static final String RESOURCE_PATH = PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH;

	@Test
	@EnabledIf("resourceExists")
	void mp3AudioFormat() {

		Audio audio = Audio.from(resource());
		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		logAudioFormat(audioFormat);

		assertThat(audioFormat).isNotNull();
		assertThat(audioFormat.getChannels()).isEqualTo(2);
		assertThat(audioFormat.getEncoding()).isEqualTo(new AudioFormat.Encoding("MPEG2L3"));
		assertThat(audioFormat.getFrameRate()).isEqualTo(22_050f);
		assertThat(audioFormat.getFrameSize()).isEqualTo(AudioUtils.unspecified());
		assertThat(audioFormat.getSampleRate()).isEqualTo(22_050f);
		assertThat(audioFormat.getSampleSizeInBits()).isEqualTo(3);
	}

	@Test
	@EnabledIf("resourceExists")
	void wavAudioFormat() {

		Audio audio = Audio.from(wavResource());
		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		logAudioFormat(audioFormat);

		assertThat(audioFormat).isNotNull();
		assertThat(audioFormat.getChannels()).isEqualTo(2);
		assertThat(audioFormat.getEncoding()).isEqualTo(new AudioFormat.Encoding("PCM_SIGNED"));
		assertThat(audioFormat.getFrameRate()).isEqualTo(44_100f);
		assertThat(audioFormat.getFrameSize()).isEqualTo(4);
		assertThat(audioFormat.getSampleRate()).isEqualTo(44_100f);
		assertThat(audioFormat.getSampleSizeInBits()).isEqualTo(16);
	}

	void logAudioFormat(AudioFormat audioFormat) {

		if (DEBUG) {
			log("AudioFormat [%s]%n", audioFormat);
			log("AudioFormat - Channels [%s]%n", audioFormat.getChannels());
			log("AudioFormat - Encoding [%s]%n", audioFormat.getEncoding());
			log("AudioFormat - FrameRate [%s]%n", audioFormat.getFrameRate());
			log("AudioFormat - FrameSize [%s]%n", audioFormat.getFrameSize());
			log("AudioFormat - SampleRate [%s]%n", audioFormat.getSampleRate());
			log("AudioFormat - SampleSizeInBits [%s]%n", audioFormat.getSampleSizeInBits());
		}
	}

	void log(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists() && wavResource().exists();
	}

	Resource wavResource() {
		return new ClassPathResource(PEARL_JAM_TEN_OCEANS);
	}
}
