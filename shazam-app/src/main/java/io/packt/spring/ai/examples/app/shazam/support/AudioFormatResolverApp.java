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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.spring.core.io.ResourceUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

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

	protected String getResourcePathArgument() {
		return this.arguments[0];
	}

	@Override
	public void run() {

		String resourcePath = getResourcePathArgument();
		Resource resource = ResourceUtils.newResource(resourcePath);
		Audio audio = Audio.from(resource);

		log.info("Supported AudioFileFormats [{}]", Arrays.stream(AudioSystem.getAudioFileTypes())
			.map(AudioFileFormat.Type::getExtension)
			.toList());

		AudioFormat audioFormat = AudioFormatBuilder.from(audio).build();

		log.info("**AudioFormat Resolver Application**");
		log.info("Audio resource [{}]", resource);
		log.info("Audio format [{}]", audioFormat);
		log.info("Audio duration [{}]", ((ShazamAudioFormat) audioFormat).getDuration());
	}
}
