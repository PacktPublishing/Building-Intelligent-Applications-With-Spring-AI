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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Java program used to resolve (introspect) the {@link AudioFileFormat} of an {@link Audio} {@link Resource}.
 *
 * @author John Blum
 * @see Audio
 * @see java.lang.Runnable
 * @see javax.sound.sampled.AudioFileFormat
 * @see javax.sound.sampled.AudioSystem
 * @since 0.1.0
 */
@Slf4j(topic = "shazam-app")
public class AudioFileFormatResolverApp implements Runnable {

	private static final String USAGE_MESSAGE = "$ java -cp <classpath> %s </path/to/audio/file[.mp3]>%n";

	public static void main(String[] args) {
		new AudioFileFormatResolverApp(validateArguments(args)).run();
	}

	private static String[] validateArguments(String[] args) {

		if (args.length < 1) {
			String message = USAGE_MESSAGE.formatted(AudioFileFormatResolverApp.class.getName());
			log.error(message);
			System.exit(-1);
		}

		return args;
	}

	private final String[] arguments;

	public AudioFileFormatResolverApp(String[] args) {
		this.arguments = args;
	}

	protected String getFirstArgument() {
		return this.arguments[0];
	}

	@Override
	public void run() {

		String resourcePath = getFirstArgument();

		try {
			Resource resource = newResource(resourcePath);
			Audio audio = newAudio(resource);
			AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(audio.inputStream());

			log.info("**AudioFileFormat Resolver Application**");
			log.info("Audio resource [{}]", resource);
			log.info("Audio file format [{}]", audioFileFormat);
			log.info("Audio file type [{}]", audioFileFormat.getType());
			log.info("Audio byte length [{}]", audioFileFormat.getByteLength());
			log.info("Audio frame length [{}]", audioFileFormat.getFrameLength());
			log.info("Audio format [{}]", audioFileFormat.getFormat());
		}
		catch (IOException | UnsupportedAudioFileException cause) {
			String message = "Failed to read file format for audio [%s]".formatted(resourcePath);
			throw AudioAccessException.because(message, cause);
		}
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
