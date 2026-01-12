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

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.provider.JavaSoundAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link AudioEmbeddingModel}.
 *
 * @author John Blum
 * @see AudioEmbeddingModel
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
@SpringBootTest
@SuppressWarnings("unused")
class AudioEmbeddingModelIntegrationTests {

	private static final String RESOURCE_PATH = "Matchbox20-Unwell.mp3";

	@Autowired
	private AudioSplitter audioSplitter;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Test
	@EnabledIf("resourceExists")
	void embedAudioClip() {

		Audio audio = Audio.from(resource());
		List<Document> documents = this.audioSplitter.split(audio);

		assertThat(documents).hasSizeGreaterThan(0);

		int index = NumberUtils.randomInt(documents.size());
		Document audioDocument = documents.get(index);

		assertThat(audioDocument)
			.isInstanceOf(AbstractDocumentStore.AudioDocument.class)
			.asInstanceOf(InstanceOfAssertFactories.type(AbstractDocumentStore.AudioDocument.class))
			.extracting(AbstractDocumentStore.AudioDocument::getAudio)
			.satisfies(this::testAudioEmbedding);
	}

	@Test
	@EnabledIf("resourceExists")
	void embedSong() {
		testAudioEmbedding(resource());
	}

	Resource resource() {
		return new ClassPathResource(RESOURCE_PATH);
	}

	boolean resourceExists() {
		return resource().exists();
	}

	void testAudioEmbedding(Resource resource) {
		testAudioEmbedding(Audio.from(resource));
	}

	void testAudioEmbedding(Audio audio) {

		Document document = AbstractDocumentStore.newAudioDocument(audio);
		float[] embedding = this.embeddingModel.embed(document);

		assertThat(embedding).isNotNull();
		assertThat(embedding).hasSize(AudioEmbeddingModel.DEFAULT_VECTOR_DIMENSIONS);
	}

	@SpringBootConfiguration
	@EnableConfigurationProperties(AudioProperties.class)
	static class TestConfiguration {

		@Bean
		JavaSoundAudioSplitter audioSplitter(AudioProperties audioProperties) {
			return new JavaSoundAudioSplitter(audioProperties);
		}

		@Bean
		AudioEmbeddingModel embeddingModel() {
			return new AudioEmbeddingModel(AbstractDocumentStore.inMemory());
		}
	}
}
