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

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Java program used to resolve (introspect) the {@link AudioFormat} of an {@link Audio} {@link Resource}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatBuilder
 * @see ShazamAudioFormat
 * @see java.lang.Runnable
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
@Slf4j(topic = "shazam-app")
public class AudioFormatResolverApp implements Runnable {

	private static final String USAGE_MESSAGE = "$ java -cp <classpath> %s </path/to/audio/file[.mp3]>%n";

	public static void main(String[] args) {
		new AudioFormatResolverApp(validateArguments(args)).run();
	}

	private static String[] validateArguments(String[] args) {

		if (args.length < 1) {
			String message = USAGE_MESSAGE.formatted(AudioFormatResolverApp.class.getName());
			log.error(message);
			System.exit(-1);
		}

		return args;
	}

	private final String[] arguments;

	public AudioFormatResolverApp(String[] args) {
		this.arguments = args;
	}

	protected String getFirstArgument() {
		return this.arguments[0];
	}

	@Override
	public void run() {

		String resourcePath = getFirstArgument();
		Resource resource = newResource(resourcePath);
		Audio audio = newAudio(resource);
		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		log.info("**AudioFormat Resolver Application**");
		log.info("Audio resource [{}]", resource);
		log.info("Audio format [{}]", audioFormat);
		log.info("Audio duration [{}]", ((ShazamAudioFormat) audioFormat).getDuration());
	}

	private Audio newAudio(Resource resource) {
		Assert.notNull(resource, "Resource is required");
		return Audio.from(resource);
	}

	private Resource newResource(String resourcePath) {
		Assert.hasText(resourcePath, "Resource path [%s] is required", resourcePath);
		Resource resource = new ClassPathResource(resourcePath);
		return resource.exists() ? resource : newFileResource(resourcePath);
	}

	private Resource newFileResource(String resourcePath) {
		Assert.hasText(resourcePath, "Resource path [%s] is required", resourcePath);
		Resource resource = new FileSystemResource(resourcePath);
		Assert.isTrue(resource.exists(), "Resource path [%s] not found", resourcePath);
		return resource;
	}
}
