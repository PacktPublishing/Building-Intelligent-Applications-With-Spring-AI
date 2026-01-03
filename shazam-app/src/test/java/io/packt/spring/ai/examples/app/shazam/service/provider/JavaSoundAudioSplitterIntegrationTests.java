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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

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
import org.tritonus.sampled.file.mpeg.MpegAudioFileWriter;

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

	private static final String AUDIO_CLIP_FILENAME = "Matchbox20-Unwell-clip.mp3";
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

		Resource mp3 = resource();

		assumeThat(mp3.exists())
			.describedAs("MP3 [%s] is not present", mp3)
			.isTrue();

		Audio audio = Audio.from(mp3);

		List<Document> documents = this.audioSplitter.split(audio);

		assertThat(documents).isNotNull();
		assertThat(documents).hasSize(91);

		long documentsSize = 0;

		for (Document document : documents) {
			Media media = document.getMedia();
			byte[] data = media.getDataAsByteArray();
			assertThat(data).hasSizeGreaterThan(0).hasSizeLessThanOrEqualTo(50_000);
			documentsSize += data.length;
		}

		// The size of the Documents in bytes should be greater than the Audio size in bytes given the overlap
		assertThat(documentsSize).isGreaterThan(audio.getData().length);

		saveAndAssertRandomAudioClip(audio, documents);
	}

	private void saveAndAssertRandomAudioClip(Audio audio, List<Document> documents) {

		int index = NumberUtils.randomInt(documents.size());

		Document document = documents.get(index);

		try (AudioInputStream in = openInputStream(audio, document)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			AudioSystem.write(in, AudioUtils.MP3_AUDIO_FILE_FORMAT, out);
			byte[] audioClipData = out.toByteArray();
			assertThat(audioClipData).hasSizeGreaterThanOrEqualTo(document.getMedia().getDataAsByteArray().length);
			saveToFile(audioClipData);
		}
		catch (IOException cause) {
			fail("Failed to write audio", cause);
		}
	}

	private AudioInputStream openInputStream(Audio audio, Document document) {

		Function<AudioInputStream, AudioFormat> audioFormatResolver = inputStream ->
			new AudioFormat(MpegAudioFileWriter.MPEG1L3, -1.0F, -1, 2, -1, -1.0F, inputStream.getFormat().isBigEndian());

		return AudioUtils.openInputStream(audio, document, audioFormatResolver, AudioInputStream::getFrameLength);
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}

	private void saveToFile(byte[] audioClipData) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(AUDIO_CLIP_FILENAME, false)) {
			fileOut.write(audioClipData);
			fileOut.flush();
		}
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
