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

import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.asInt;
import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.asLong;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.function.Function;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for Java Sound {@link AudioInputStream}.
 *
 * @author John Blum
 * @see AbstractShazamIntegrationTests
 * @see javax.sound.sampled.AudioInputStream
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class AudioInputStreamIntegrationTests extends AbstractShazamIntegrationTests {

	private static final String MATCHBOX20_UNWELL_RESOURCE_PATH = "Matchbox20-Unwell.mp3";
	private static final String PEARL_JAM_TEN_JEREMY_RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";
	private static final String RESOURCE_PATH = PEARL_JAM_TEN_JEREMY_RESOURCE_PATH;

	private static final NumberFormat numberFormat = NumberFormat.getNumberInstance();

	static {
		numberFormat.setGroupingUsed(true);
	}

	@Test
	@EnabledIf("mp3ResourceExists")
	void matchbox20Unwell() throws IOException, UnsupportedAudioFileException {

		Audio audio = Audio.from(mp3Resource());

		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio.inputStream())) {
			assertAudioInputStream(audio, audioInputStream, AudioUtils.MPEG_TWO_LAYER_THREE_ENCODING);
		}
	}

	@Test
	@EnabledIf("resourceExists")
	void pearlJamTenJeremy() throws IOException, UnsupportedAudioFileException {

		Audio audio = Audio.from(resource());

		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio.inputStream())) {
			assertAudioInputStream(audio, audioInputStream, AudioFormat.Encoding.PCM_SIGNED, this::computeFrameLength);
		}
	}

	private void assertAudioInputStream(Audio audio, AudioInputStream audioInputStream, AudioFormat.Encoding encoding) {
		assertAudioInputStream(audio, audioInputStream, encoding, audioFormat -> asLong(AudioSystem.NOT_SPECIFIED));
	}

	private void assertAudioInputStream(Audio audio, AudioInputStream audioInputStream,
			AudioFormat.Encoding encoding, Function<ShazamAudioFormat, Long> frameLength) {

		assertThat(audioInputStream).isNotNull();
		assertThat(audioInputStream.getFormat()).isNotNull();
		assertThat(audioInputStream.getFormat().getEncoding()).isEqualTo(encoding);

		ShazamAudioFormat audioFormat = ShazamAudioFormat.from(audio, audioInputStream.getFormat());

		NumberFormat numberFormat = NumberFormat.getNumberInstance();

		numberFormat.setGroupingUsed(true);

		long expectedFrameLength = frameLength.apply(audioFormat);
		long actualFrameLength = audioInputStream.getFrameLength();
		long diff = actualFrameLength - expectedFrameLength;

		assertThat(actualFrameLength)
			.describedAs("Expected Frame Length of [%s]; but was [%s]; diff of [%s]",
				format(expectedFrameLength), format(actualFrameLength), format(diff))
			.isGreaterThanOrEqualTo(expectedFrameLength);
	}

	private long computeFrameLength(ShazamAudioFormat audioFormat) {
		Duration duration = audioFormat.getDuration();
		int frameRate = asInt(audioFormat.getFrameRate());
		return frameRate * duration.toSeconds();
	}

	private String format(long number) {
		return numberFormat.format(number);
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	Resource mp3Resource() {
		return new ClassPathResource(MATCHBOX20_UNWELL_RESOURCE_PATH);
	}

	boolean mp3ResourceExists() {
		return mp3Resource().exists();
	}
}
