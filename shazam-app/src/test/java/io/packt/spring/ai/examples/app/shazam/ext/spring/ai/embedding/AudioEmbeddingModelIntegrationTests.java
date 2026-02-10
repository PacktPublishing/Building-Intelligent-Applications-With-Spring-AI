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
package io.packt.spring.ai.examples.app.shazam.ext.spring.ai.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.config.ShazamConfiguration;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.provider.JavaSoundAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests for {@link AudioEmbeddingModel}.
 *
 * @author John Blum
 * @see AudioEmbeddingModel
 * @see AbstractShazamIntegrationTests
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles("honerlaw")
@SuppressWarnings("unused")
class AudioEmbeddingModelIntegrationTests extends AbstractShazamIntegrationTests {

	private static final String AUDIO_CLIP_RESOURCE_PATH = "PearlJam-Ten-Jeremy-clip-1s.wav";
	private static final String SONG_RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";
	private static final String RESOURCE_PATH = AUDIO_CLIP_RESOURCE_PATH;

	@Autowired
	private AudioSplitter audioSplitter;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Test
	void embedAudioClip() {
		assertAudioEmbedding(resource());
	}

	@Test
	@EnabledIf("resourceExists")
	void embedAudioClipFromAudioSplitter() {

		Audio audio = Audio.from(resource());
		List<Document> documents = this.audioSplitter.split(audio);

		assertThat(documents).hasSizeGreaterThan(0);

		int index = NumberUtils.randomInt(documents.size());
		Document audioDocument = documents.get(index);

		assertThat(audioDocument)
			.isInstanceOf(AbstractDocumentStore.AudioDocument.class)
			.asInstanceOf(InstanceOfAssertFactories.type(AbstractDocumentStore.AudioDocument.class))
			.extracting(AbstractDocumentStore.AudioDocument::getAudio)
			.satisfies(this::assertAudioEmbedding);
	}

	@Test
	@EnabledIf("resourceExists")
	@Disabled("Creating embedding from Fingerprint generated from Audio for an entire Song is not efficient")
	void embedSong() {
		assertAudioEmbedding(resource(SONG_RESOURCE_PATH));
	}

	void assertAudioEmbedding(Resource resource) {
		assertAudioEmbedding(Audio.from(resource));
	}

	void assertAudioEmbedding(Audio audio) {

		Document document = AbstractDocumentStore.newAudioDocument(audio);
		float[] embedding = this.embeddingModel.embed(document);

		assertThat(embedding).isNotNull();
		assertThat(embedding).hasSize(this.embeddingModel.dimensions());
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ShazamConfiguration.class)
	static class TestConfiguration {

		@Bean
		AudioSplitter audioSpitter(AudioProperties audioProperties) {
			return new JavaSoundAudioSplitter(audioProperties);
		}
	}
}
