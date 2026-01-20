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
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.ext.tarsos.MpegAudioFormatBuilder;
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

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.printf("> java -cp <classpath> %s </path/to/audio/file[.mp3]>%n",
				AudioFormatResolverApp.class.getName());
			System.exit(-1);
		}

		new AudioFormatResolverApp(args).run();
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

		Resource resource = newResource(getFirstArgument());

		if (!resource.exists()) {
			printError("Audio resource [%s] does not exist%n", resource);
			System.exit(-2);
		}

		Audio audio = newAudio(resource);

		ExceptionThrowingRunnable.runSafely(() -> {
			try (AudioInputStream audioInputStream = openInputStream(audio)) {
				AudioFormat audioFormat = audioInputStream.getFormat();
				printOut("Audio [%s] format [%s]%n", resource, audioFormat);
				printOut("Audio duration [%s]%n", ((ShazamAudioFormat) audioFormat).getDuration());
			}
			catch (IOException cause) {
				printError("Failed to determine the format of audio [%s] because: %s%n", resource, cause.getMessage());
				debug(() -> printError(cause));
				System.exit(-3);
			}
		});
	}

	private static Audio newAudio(Resource resource) {
		Assert.isTrue(resource.isFile(), "Expecting resource [%s] to originate from file", resource);
		File file = ExceptionThrowingSupplier.getSafely(resource::getFile);
		Audio audio = Audio.from(file);
		AudioFormat audioFormat = MpegAudioFormatBuilder.mpegOneLayerThree(audio).build();
		audio = audio.in(audioFormat);
		return audio;
	}

	private static Resource newResource(String resourcePath) {
		return new ClassPathResource(resourcePath);
	}

	private static AudioInputStream openInputStream(Audio audio) {
		return AudioInputStreamBuilder.from(audio).build();
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
