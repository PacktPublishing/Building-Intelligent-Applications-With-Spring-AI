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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.codeprimate.extensions.util.ExceptionThrowingRunnable;
import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatResolver;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Java program used to resolve (introspect) the {@link AudioFormat} of an {@link Audio} {@link Resource}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatBuilder
 * @see java.lang.Runnable
 * @see javax.sound.sampled.AudioFormat
 * @see org.springframework.core.io.Resource
 * @since 0.1.0
 */
public class AudioFormatResolverApp implements Runnable {

	private static final boolean DEBUG = false;

	private static final String USAGE_MESSAGE = "$ java -cp <classpath> %s </path/to/audio/file[.mp3]>%n";

	public static void main(String[] args) {
		new AudioFormatResolverApp(validateArguments(args)).run();
	}

	private static String[] validateArguments(String[] args) {

		if (args.length < 1) {
			String message = USAGE_MESSAGE.formatted(AudioFormatResolverApp.class.getName());
			System.err.printf(message);
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

		if (!resource.exists()) {
			printError("Audio resource [%s] not found%n", resource);
			System.exit(-2);
		}

		Audio audio = newAudio(resource);

		ExceptionThrowingRunnable.runSafely(() -> {
			try (AudioInputStream audioInputStream = openInputStream(audio)) {
				AudioFormat audioFormat = audioInputStream.getFormat();
				printOut("Audio resource [%s]", resource);
				printOut("Audio format [%s]%n", audioFormat);
				printOut("Audio duration [%s]%n", ((ShazamAudioFormat) audioFormat).getDuration());
			}
			catch (IOException cause) {
				printError("Failed to determine the format of audio [%s] because: %s%n", resource, cause.getMessage());
				debug(() -> printError(cause));
				System.exit(-3);
			}
		});
	}

	private Audio newAudio(Resource resource) {
		Assert.notNull(resource, "Resource is required");
		Assert.isTrue(resource.isFile(), "Expecting resource [%s] to originate from file", resource);
		File file = ExceptionThrowingSupplier.getSafely(resource::getFile);
		Audio audio = Audio.from(file);
		audio = audio.in(resolveAudioFormat(audio));
		return audio;
	}

	private Resource newResource(String resourcePath) {
		Assert.hasText(resourcePath, "Resource path [%s] is required", resourcePath);
		return new ClassPathResource(resourcePath);
	}

	private AudioInputStream openInputStream(Audio audio) {
		return AudioInputStreamBuilder.from(audio).build();
	}

	private AudioFormat resolveAudioFormat(Audio audio) {
		return AudioFormatResolver.defaultAudioFormatResolver().resolve(audio);
	}

	private void print(PrintStream out, String message, Object... arguments) {
		out.printf(message, arguments);
		out.flush();
	}

	private void printOut(String message, Object... arguments) {
		print(System.out, message, arguments);
	}

	private void printError(String message, Object... arguments) {
		print(System.err, message, arguments);
	}

	private void printError(Throwable error) {
		error.printStackTrace(System.err);
	}

	private void debug(Runnable action) {
		if (DEBUG) {
			action.run();
		}
	}
}
