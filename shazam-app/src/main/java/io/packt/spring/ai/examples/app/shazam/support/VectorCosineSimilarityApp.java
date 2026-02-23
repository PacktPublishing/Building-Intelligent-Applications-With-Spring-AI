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
package io.packt.spring.ai.examples.app.shazam.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintEmbeddingFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.DefaultAudioFingerprintEmbeddingFunction;
import io.packt.spring.ai.examples.app.shazam.ext.spring.ai.embedding.AudioEmbeddingModel;
import io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp.SpectrogramAudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.provider.JavaSoundAudioSplitter;

import org.cp.elements.lang.Assert;
import org.cp.elements.util.PropertiesAdapter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Java program using {@literal Cosine Similarity} to compare {@literal Vectors}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioEmbeddingModel
 * @see java.lang.Runnable
 * @since 0.1.0
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public class VectorCosineSimilarityApp implements Runnable {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.printf("> java -cp <classpath> %s <song-resource> <audio-clip-resource>%n",
				VectorCosineSimilarityApp.class.getName());
			System.exit(-1);
		}

		new VectorCosineSimilarityApp(args).run();
	}

	private final ApplicationArguments applicationArguments;

	private final AudioSplitter audioSplitter;

	private final EmbeddingModel embeddingModel;

	private final PropertiesAdapter applicationProperties;

	public VectorCosineSimilarityApp(String[] args) {
		this.applicationArguments = Factory.newApplicationArguments(args);
		this.applicationProperties = Factory.loadApplicationProperties();
		this.audioSplitter = Factory.newAudioSplitter();
		this.embeddingModel = Factory.newEmbeddingModel();
	}

	@Override
	public void run() {

		Audio song = Audio.from(Factory.newResource(getApplicationArguments().getSourceArgs()[0]));
		Audio audioClip = Audio.from(Factory.newResource(getApplicationArguments().getSourceArgs()[1]));

		Document audioClipDocument = AbstractDocumentStore.newAudioDocument(audioClip);

		double similarityThreshold =
			getApplicationProperties().getAsType("shazam.song.search.similarity-threshold", Double.class);

		float[] audioClipVector = getEmbeddingModel().embed(audioClipDocument);

		int matchCount = 0;
		int uniqueVectorCount = 0;

		Set<Integer> vectorHashCodes = new HashSet<>();

		for (Document songDocument : getAudioSplitter().split(song)) {
			float[] songVector = getEmbeddingModel().embed(songDocument);
			double cosineSimilarity = SimpleVectorStore.EmbeddingMath.cosineSimilarity(audioClipVector, songVector);
			matchCount += cosineSimilarity > similarityThreshold ? 1 : 0;
			uniqueVectorCount += vectorHashCodes.add(Arrays.hashCode(songVector)) ? 1 : 0;
			System.out.printf("Similarity = %s%n%n", cosineSimilarity);
		}

		System.out.printf("Unique Song Vector Count [%d]%n", uniqueVectorCount);
		System.out.printf("Number of Matches [%d]", matchCount);
	}

	static class Factory {

		static final String APPLICATION_PROPERTIES = "application.properties";

		static PropertiesAdapter loadApplicationProperties() {

			Properties applicationProperties = new Properties();

			try (InputStream in = Factory.class.getResourceAsStream(APPLICATION_PROPERTIES)) {
				applicationProperties.load(in);
			}
			catch (IOException cause) {
				log.warn("IO error occurred while loading application.properties", cause);
			}

			return PropertiesAdapter.from(applicationProperties);
		}

		static ApplicationArguments newApplicationArguments(String[] args) {
			return new DefaultApplicationArguments(args);
		}

		static AudioFingerprintFunction<?> newAudioFingerprintFunction() {
			return new SpectrogramAudioFingerprintFunction();
		}

		static AudioFingerprintEmbeddingFunction newAudioFingerprintEmbeddingFunction() {
			return new DefaultAudioFingerprintEmbeddingFunction();
		}

		static AudioSplitter newAudioSplitter() {
			return new JavaSoundAudioSplitter(AudioProperties.defaultAudioProperties());
		}

		static EmbeddingModel newEmbeddingModel() {
			return new AudioEmbeddingModel(newAudioFingerprintFunction(), newAudioFingerprintEmbeddingFunction(),
				AbstractDocumentStore.inMemory());
		}

		static Resource newResource(String resourcePath) {
			Resource resource = new ClassPathResource(resourcePath);
			resource = resource.exists() ? resource : new FileSystemResource(resourcePath);
			Assert.isTrue(resource.exists(), "Resource [%s] not found");
			return resource;
		}
	}
}
