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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.Resource;

/**
 * Integration Tests for Java Sound {@link AudioFileFormat}.
 *
 * @author John Blum
 * @see Audio
 * @see AbstractShazamIntegrationTests
 * @see javax.sound.sampled.AudioFileFormat
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
@SuppressWarnings("unused")
class AudioFileFormatIntegrationTests extends AbstractShazamIntegrationTests {

	private static final String MATCHBOX20_UNWELL_RESOURCE_PATH = "Matchbox20-Unwell.mp3"; // Internet

	// Throws UnsupportedAudioFileException: File of unsupported format
	private static final String PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH = "PearlJam-NoCode-RedMosquito.mp3"; // Album

	private static final String PEARL_JAM_TEN_JEREMY_RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	//private static final String RESOURCE_PATH = PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH;
	private static final String RESOURCE_PATH = MATCHBOX20_UNWELL_RESOURCE_PATH;

	@Test
	@EnabledIf("resourceExists")
	void mp3AudioFileFormat() throws UnsupportedAudioFileException, IOException {

		Audio audio = Audio.from(resource());
		AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(audio.file());

		assertThat(audioFileFormat).isNotNull();
		assertThat(audioFileFormat.getByteLength()).isEqualTo(audio.getData().length);
		assertThat(audioFileFormat.getFrameLength()).isEqualTo(AudioSystem.NOT_SPECIFIED);
		assertThat(audioFileFormat.getType().getExtension()).isEqualTo("mpeg");
		assertThat(audioFileFormat.properties()).isNull();
		assertAudioFormat(audioFileFormat.getFormat(), new AudioFormat.Encoding("MPEG2L3"));
	}

	@Test
	@EnabledIf("wavResourceExists")
	void wavAudioFileFormat() throws UnsupportedAudioFileException, IOException {

		Audio audio = Audio.from(wavResource());
		AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(audio.file());

		assertThat(audioFileFormat).isNotNull();
		assertThat(audioFileFormat.getByteLength()).isEqualTo(audio.getData().length);
		assertThat(audioFileFormat.getFrameLength()).isGreaterThan(0); // TODO: ?
		assertThat(audioFileFormat.getType()).isEqualTo(AudioFileFormat.Type.WAVE);
		assertThat(audioFileFormat.properties()).isEmpty();
		assertAudioFormat(audioFileFormat.getFormat(), AudioFormat.Encoding.PCM_SIGNED);
	}

	void assertAudioFormat(AudioFormat audioFormat, AudioFormat.Encoding encoding) {
		assertThat(audioFormat).isNotNull();
		assertThat(audioFormat.getEncoding()).isEqualTo(encoding);
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	Resource wavResource() {
		return resource(PEARL_JAM_TEN_JEREMY_RESOURCE_PATH);
	}

	boolean wavResourceExists() {
		return wavResource().exists();
	}
}
