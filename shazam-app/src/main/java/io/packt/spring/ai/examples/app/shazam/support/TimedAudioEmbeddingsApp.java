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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.util.AbstractTimer;
import io.codeprimate.extensions.util.Timer;
import io.honerlaw.audio.fingerprint.hash.peak.HashedPeak;
import io.packt.spring.ai.examples.app.shazam.config.ShazamConfiguration;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.Fingerprint;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.util.DocumentUtils;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ClassUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} that computes {@link Embedding Embeddings} for {@link Audio}
 * and measures the {@link Duration} of time taken to compute the {@link Embedding Embeddings}.
 *
 * @author John Blum
 * @see Audio
 * @see SpringBootApplication
 * @see SpringBootConfiguration
 * @see AbstractSpringBootApplication
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.ai.embedding.Embedding
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @since 1.0.0
 */
@SpringBootApplication
@Slf4j(topic = "shazam-app")
@Profile(TimedAudioEmbeddingsApp.AUDIO_EMBEDDING_APP_PROFILE)
public class TimedAudioEmbeddingsApp extends AbstractSpringBootApplication {

	static final String AUDIO_EMBEDDING_APP_PROFILE = "audio-embeddings-app";

	public static void main(String[] args) {
		String[] activeProfiles = asStringArray(AUDIO_EMBEDDING_APP_PROFILE, "honerlaw");
		runSpringApplication(TimedAudioEmbeddingsApp.class, activeProfiles, applicationBuilderFunction(), args);
	}

	private static Function<SpringApplicationBuilder, SpringApplicationBuilder> applicationBuilderFunction() {
		return springApplicationBuilder ->
			springApplicationBuilder.properties(Collections.singletonMap("spring.docker.compose.enabled", "false"));
	}

	@SpringBootConfiguration
	@Import(ShazamConfiguration.class)
	static class TimedAudioEmbeddingsConfiguration {

		@Bean
		AudioEmbeddingContext audioEmbeddingContext(
			AudioSplitter audioSplitter,
			AudioFingerprintFunction<Object> function,
			OllamaEmbeddingModel embeddingModel
		) {
			return AudioEmbeddingContext.from(audioSplitter, function, embeddingModel);
		}

		@Bean
		AbstractAudioEmbeddingsService audioEmbeddingService(AudioEmbeddingContext context) {
			return AbstractAudioEmbeddingsService.from(context);
		}
	}

	@Bean
	ApplicationRunner programRunner(AbstractAudioEmbeddingsService audioEmbeddingService) {

		return args -> {

			String resourcePath  = args.getSourceArgs()[0];
			Resource audioResource = newResource(resourcePath);
			Audio audio = Audio.from(audioResource);

			print("Timing the generation of embeddings for audio [%s]...%n", resourcePath);

			Duration duration = audioEmbeddingService.process(audio);

			print("Audio Embeddings generated in [%s]%n", toString(duration));
		};
	}

	static Resource newResource(String resourcePath) {
		Assert.hasText(resourcePath, "Resource path [%s] is required", resourcePath);
		Resource resource = new ClassPathResource(resourcePath);
		resource = resource.exists() ? resource : new FileSystemResource(resourcePath);
		Assert.isTrue(resource.exists(), "Resource [%s] not found", resourcePath);
		return resource;
	}

	static String toString(Duration duration) {
		long milliseconds = duration.toMillis();
		long seconds = milliseconds / 1000;
		milliseconds = milliseconds % 1000;
		return "%s s %s ms".formatted(seconds, milliseconds);
	}

	interface AudioEmbeddingContext {

		static AudioEmbeddingContext from(
			AudioSplitter audioSplitter,
			AudioFingerprintFunction<Object> audioFingerprintFunction,
			EmbeddingModel embeddingModel
		) {

			Assert.notNull(audioSplitter, "AudioSplitter is required");
			Assert.notNull(audioFingerprintFunction, "AudioFingerprintFunction is required");
			Assert.notNull(embeddingModel, "EmbeddingModel is required");

			return new AudioEmbeddingContext() {

				@Override
				public AudioSplitter getAudioSplitter() {
					return audioSplitter;
				}

				@Override
				public AudioFingerprintFunction<Object> getAudioFingerprintFunction() {
					return audioFingerprintFunction;
				}

				@Override
				public EmbeddingModel getEmbeddingModel() {
					return embeddingModel;
				}
			};
		}

		AudioSplitter getAudioSplitter();

		AudioFingerprintFunction<Object> getAudioFingerprintFunction();

		EmbeddingModel getEmbeddingModel();

	}

	static abstract class AbstractAudioEmbeddingsService {

		static AbstractAudioEmbeddingsService from(AudioEmbeddingContext context) {

			Assert.notNull(context, "AudioEmbeddingContext is required");

			return new AbstractAudioEmbeddingsService() {

				@Override
				AudioEmbeddingContext getContext() {
					return context;
				}
			};
		}

		abstract AudioEmbeddingContext getContext();

		AudioFingerprintFunction<Object> getAudioFingerprintFunction() {
			return getContext().getAudioFingerprintFunction();
		}

		AudioSplitter getAudioSplitter() {
			return getContext().getAudioSplitter();
		}

		EmbeddingModel getEmbeddingModel() {
			return getContext().getEmbeddingModel();
		}

		Embedding embed(Document document) {
			float[] vector = getEmbeddingModel().embed(document);
			return new Embedding(vector, 0);
		}

		Fingerprint<Object> fingerprint(Audio audio) {
			return getAudioFingerprintFunction().compute(audio);
		}

		List<Document> split(Audio audio) {
			return getAudioSplitter().split(audio);
		}

		Duration process(Audio audio) {
			Timer<Audio, ?> timer = newTimer();
			timer.run(audio);
			return timer.getTime();
		}

		private Timer<Audio, ?> newTimer() {

			return AbstractTimer.time(audio -> {

				List<Document> audioDocuments = split(audio);

				audioDocuments = audioDocuments.subList(0, audioDocuments.size() - 1);

				log.info("[{}] Audio Clip(s)", audioDocuments.size());

				List<Fingerprint<Object>> fingerprints = audioDocuments.stream()
					.map(DocumentUtils::toAudio)
					.filter(Audio::isNotEmpty)
					.map(this::fingerprint)
					.toList();

				log.info("[{}] Audio Fingerprints", fingerprints.size());

				List<Document> fingerprintDocuments = new ArrayList<>();

				for (Fingerprint<Object> fingerprint : fingerprints) {

					List<String> hashes = new ArrayList<>();

					fingerprint.forEach(hash -> {

						if (hash instanceof HashedPeak hashedPeak) {
							hashes.add(hashedPeak.getHashAsHex());
							return;
						}

						throw new IllegalStateException("Hash [%s] was not a HashedPeak"
							.formatted(ClassUtils.getClassName(hash)));
					});

					hashes.stream()
						.map(hash -> Document.builder().text(hash).build())
						.forEach(fingerprintDocuments::add);
				}

				log.info("[{}] Audio Embeddings", fingerprintDocuments.size());

				AtomicLong counter = new AtomicLong(0L);
				long startTime = System.currentTimeMillis();

				fingerprintDocuments.forEach(document -> {
					embed(document);
					if (counter.incrementAndGet() % 100 == 0) {
						log.info("Embedded [{}] Documents in [{}]", counter.get(),
							TimedAudioEmbeddingsApp.toString(Duration.ofMillis(System.currentTimeMillis() - startTime)));
					}
				});
			});
		}
	}
}
