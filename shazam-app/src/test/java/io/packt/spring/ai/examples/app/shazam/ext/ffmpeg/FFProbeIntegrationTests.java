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
package io.packt.spring.ai.examples.app.shazam.ext.ffmpeg;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link FFProbe}.
 *
 * @author John Blum
 * @see FFProbe
 * @see AbstractShazamIntegrationTests
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
class FFProbeIntegrationTests extends AbstractShazamIntegrationTests {

	private static final String MATCHBOX20_UNWELL_RESOURCE_PATH = "Matchbox20-Unwell.mp3"; // Internet
	private static final String PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH = "PearlJam-NoCode-RedMosquito.mp3"; // Album
	private static final String PEARL_JAM_TEN_JEREMY_RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav"; // Album

	@Test
	@EnabledIf("matchbox20UnwellResourceExists")
	void matchbox20UnwellAudioFormat() {

		Audio audio = Audio.from(matchbox20UnwellResource());

		FFProbe probe = FFProbe.builder()
			.enableDownload()
			.build();

		FFProbe.Format format = probe.showFormat(audio);

		assertThat(format).isNotNull();
		assertThat(format.bitRate()).isEqualTo(80_000);
		assertThat(format.getDuration()).isGreaterThan(Duration.ofMinutes(3).plusSeconds(48));
		assertThat(format.filename()).isEqualTo(audio.file().getAbsolutePath());
		assertThat(format.name().toLowerCase()).isEqualTo("mp3");
		assertThat(format.description()).contains("MPEG audio layer 2/3");
		assertThat(format.numberOfPrograms()).isZero();
		assertThat(format.numberOfStreams()).isOne();
		assertThat(format.numberOfStreamGroups()).isZero();
		assertThat(format.probeScore()).isEqualTo(51); // ?
		assertThat(format.size()).isGreaterThan(2_200_000);
		assertThat(format.startTime()).isEqualTo(0.0d);
	}

	@Test
	@EnabledIf("pearlJamNoCodeRedMosquitoResourceExists")
	void pearlJamNoCodeRedMosquitoAudioFormat() {

		Audio audio = Audio.from(pearlJamNoCodeRedMosquitoResource());

		FFProbe probe = FFProbe.builder()
			.enableDownload()
			.build();

		FFProbe.Format format = probe.showFormat(audio);

		assertThat(format).isNotNull();
		assertThat(format.bitRate()).isGreaterThanOrEqualTo(160_000);
		assertThat(format.getDuration()).isGreaterThan(Duration.ofMinutes(4).plusSeconds(3));
		assertThat(format.filename()).isEqualTo(audio.file().getAbsolutePath());
		assertThat(format.name().toLowerCase()).isEqualToIgnoringCase("MP3");
		assertThat(format.description()).contains("MPEG audio layer 2/3");
		assertThat(format.numberOfPrograms()).isZero();
		assertThat(format.numberOfStreams()).isOne();
		assertThat(format.numberOfStreamGroups()).isZero();
		assertThat(format.probeScore()).isEqualTo(51); // ?
		assertThat(format.size()).isGreaterThan(4_800_000);
		assertThat(format.startTime()).isEqualTo(0.0d);
	}

	@Test
	@EnabledIf("pearlJamTenJeremyResourceExists")
	void pearlJamTenJeremyAudioFormat() {

		Audio audio = Audio.from(pearlJamTenJeremyResource());

		FFProbe probe = FFProbe.builder()
			.enableDownload()
			.build();

		FFProbe.Format format = probe.showFormat(audio);

		assertThat(format).isNotNull();
		assertThat(format.bitRate()).isGreaterThanOrEqualTo(160_000);
		assertThat(format.getDuration()).isGreaterThan(Duration.ofMinutes(5).plusSeconds(18));
		assertThat(format.filename()).isEqualTo(audio.file().getAbsolutePath());
		assertThat(format.name().toLowerCase()).isEqualToIgnoringCase("WAV");
		assertThat(format.description()).contains("WAV / WAVE (Waveform Audio)");
		assertThat(format.numberOfPrograms()).isZero();
		assertThat(format.numberOfStreams()).isOne();
		assertThat(format.numberOfStreamGroups()).isZero();
		assertThat(format.probeScore()).isEqualTo(99); // TODO: ?
		assertThat(format.size()).isGreaterThan(56_300_000);
 		assertThat(format.startTime()).isNull(); // TODO: ?
	}

	@Override
	protected String resourcePath() {
		return MATCHBOX20_UNWELL_RESOURCE_PATH;
	}

	Resource matchbox20UnwellResource() {
		return resource(MATCHBOX20_UNWELL_RESOURCE_PATH);
	}

	boolean matchbox20UnwellResourceExists() {
		return matchbox20UnwellResource().exists();
	}

	Resource pearlJamNoCodeRedMosquitoResource() {
		return resource(PEARL_JAM_NO_CODE_RED_MOSQUITO_RESOURCE_PATH);
	}

	boolean pearlJamNoCodeRedMosquitoResourceExists() {
		return pearlJamNoCodeRedMosquitoResource().exists();
	}

	Resource pearlJamTenJeremyResource() {
		return resource(PEARL_JAM_TEN_JEREMY_RESOURCE_PATH);
	}

	boolean pearlJamTenJeremyResourceExists() {
		return pearlJamTenJeremyResource().exists();
	}
}
