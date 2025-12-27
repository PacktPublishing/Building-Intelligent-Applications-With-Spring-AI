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

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link FFProbe}.
 *
 * @author John Blum
 * @see FFProbe
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class FFProbeIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Test
	@EnabledIf("resourceExists")
	void audioFormatIsCorrect() {

		Audio audio = Audio.from(resource());

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

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}
}
