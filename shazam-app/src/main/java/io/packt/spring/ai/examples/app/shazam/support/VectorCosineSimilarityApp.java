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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.spring.ai.embedding.AudioEmbeddingModel;
import io.packt.spring.ai.examples.app.shazam.ext.tarsos.dsp.SpectrogramAudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.ext.tritonous.MpegAudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.provider.JavaSoundAudioSplitter;

import org.cp.elements.lang.Assert;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Java program using {@literal Cosine Similarity} to compare {@literal Vectors}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioEmbeddingModel
 * @see java.lang.Runnable
 * @since 0.1.0
 */
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

	private final AudioSplitter audioSplitter;
	private final EmbeddingModel embeddingModel;
	private final String[] arguments;

	public VectorCosineSimilarityApp(String[] args) {
		this.arguments = args;
		this.audioSplitter = newAudioSplitter();
		this.embeddingModel = newEmbeddingModel();
	}

	protected AudioSplitter newAudioSplitter() {
		return new JavaSoundAudioSplitter(AudioProperties.defaultAudioProperties());
	}

	protected EmbeddingModel newEmbeddingModel() {
		return new AudioEmbeddingModel(newAudioFingerprintFunction(), AbstractDocumentStore.inMemory());
	}

	private AudioFingerprintFunction newAudioFingerprintFunction() {
		//return new MfccAudioFingerprintFunction();
		return new SpectrogramAudioFingerprintFunction();
	}

	protected String getAudioClipArgument() {
		return getArguments()[1];
	}

	protected String getSongArgument() {
		return getArguments()[0];
	}

	@Override
	public void run() {

		Audio song = Audio.from(newResource(getSongArgument()));
		Audio audioClip = Audio.from(newResource(getAudioClipArgument()));

		AudioFormat audioFormat = AudioFormatBuilder.from(audioClip)
			.defaultAudioFormat(() -> MpegAudioFormatBuilder.mpegOneLayerThree(audioClip)
				//.withSampleRateOf44100()
				.withSampleRateOf22050()
				.build())
			.build();

		audioClip.in(audioFormat);

		Document audioClipDocument = AbstractDocumentStore.newAudioDocument(audioClip);

		float[] audioClipVector = getEmbeddingModel().embed(audioClipDocument);

		int count = 0;
		int uniqueVectorCount = 0;

		Set<Integer> vectorHashCodes = new HashSet<>();

		System.out.printf("%n%nVector for audio clip [%s] is (%s)%n%n",
			getAudioClipArgument(), Arrays.toString(audioClipVector));

		System.out.printf("Vectors for song [%s] are:%n%n", getSongArgument());

		for (Document songDocument : getAudioSplitter().split(song)) {
			float[] songVector = getEmbeddingModel().embed(songDocument);
			double cosineSimilarity = SimpleVectorStore.EmbeddingMath.cosineSimilarity(audioClipVector, songVector);
			uniqueVectorCount += vectorHashCodes.add(Arrays.hashCode(songVector)) ? 1 : 0;
			System.out.printf("%d - Vector (%s)%n", ++count, Arrays.toString(songVector));
			System.out.printf("Similarity = %s%n%n", cosineSimilarity);
		}

		System.out.printf("Unique Song Vector Count [%d]%n", uniqueVectorCount);
	}

	private Resource newResource(String resourcePath) {
		Resource resource = new FileSystemResource(resourcePath);
		Assert.isTrue(resource.exists(), "Resource [%s] not found");
		return resource;
	}
}
