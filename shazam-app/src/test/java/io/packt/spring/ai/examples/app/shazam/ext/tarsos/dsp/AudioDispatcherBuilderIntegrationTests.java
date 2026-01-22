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
package io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.core.io.ClassPathResource;

import be.tarsos.dsp.AudioDispatcher;

/**
 * Integration Tests for {@link AudioDispatcherBuilder}.
 *
 * @author John Blum
 * @see AudioDispatcherBuilder
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
class AudioDispatcherBuilderIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Test
	@EnabledIf("resourceExists")
	void mfccProcessingFinishedSuccessfully() throws Exception {

		Audio audio = Audio.from(new ClassPathResource(RESOURCE_PATH));

		assertAudioMetadata(audio);

		Consumer<float[]> mfccConsumer = mfcc -> {
			assertThat(mfcc).isNotEmpty();
			System.out.printf("MFCC size [%d]%n", mfcc.length);
			//System.out.printf("MFCC array %s%n", Arrays.toString(mfcc));
			assertThat(mfcc).hasSizeLessThan(audio.getData().length);
		};

		AudioDispatcher audioDispatcher = AudioDispatcherBuilder.from(audio)
			.withNumberOfCepstrumCoefficients(45)
			.registerMFCC(mfccConsumer)
			.build();

		assertThat(audioDispatcher).isNotNull();

		audioDispatcher.run();
	}

	private void assertAudioMetadata(Audio audio) throws IOException, UnsupportedAudioFileException {

		assertThat(audio)
			.describedAs("Audio for [%s] is required", RESOURCE_PATH)
			.isNotNull();

		AudioInputStream inputStream = AudioSystem.getAudioInputStream(audio.inputStream());

		assertThat(inputStream).isNotNull();
		assertThat(inputStream).isNotEmpty();

		AudioFormat format = inputStream.getFormat();

		log("Audio Encoding [%s]%n", format.getEncoding());

		assertThat(format).isNotNull();
		assertThat(format.getChannels()).isEqualTo(2); // Stereo
		assertThat(format.getEncoding()).describedAs("Audio Encoding is required").isNotNull();
		assertThat(format.getEncoding().toString().toUpperCase()).isEqualTo("MPEG2L3");
		//assertThat(audioFormat.getFrameRate()).isGreaterThan(0.0f); // -1.0f for MP3
		//assertThat(audioFormat.getFrameSize()).isGreaterThan(0); // -1 for MP3
		assertThat(format.getSampleRate()).isGreaterThan(0.0f);
		assertThat(format.getSampleRate()).isGreaterThanOrEqualTo(22_050f);
		//assertThat(format.getSampleSizeInBits()).isGreaterThan(0);
	}

	void log(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	boolean resourceExists() {
		return new ClassPathResource(RESOURCE_PATH).exists();
	}
}
