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
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingRunnable;
import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.tritonus.sampled.file.mpeg.MpegAudioFileWriter;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Java program used to inspect the {@link AudioFormat} of an {@link Audio} {@link Resource}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatBuilder
 * @see java.lang.Runnable
 * @see javax.sound.sampled.AudioFormat
 * @see org.springframework.core.io.Resource
 * @since 0.1.0
 */
public class AudioFormatIntrospectorApp implements Runnable {

	private static final boolean DEBUG = false;

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.printf("> java -cp <classpath> %s </path/to/audio/file[.mp3]>%n",
				AudioFormatIntrospectorApp.class.getName());
			System.exit(-1);
		}

		new AudioFormatIntrospectorApp(args).run();
	}

	private final String[] arguments;

	public AudioFormatIntrospectorApp(String[] args) {
		this.arguments = args;
	}

	protected String getFirstArgument() {
		return arguments[0];
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
		AudioFormat audioFormat = EncodedAudioFormatBuilder.mpegOneLayerThree(audio).build();
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

	@SuppressWarnings("unused")
	@Getter(AccessLevel.PROTECTED)
	static class EncodedAudioFormatBuilder implements Builder<AudioFormat> {

		static EncodedAudioFormatBuilder mpegOneLayerThree(Audio audio) {
			return new EncodedAudioFormatBuilder(audio, MpegAudioFileWriter.MPEG1L3);
		}

		private final Audio audio;

		private final AudioFormat.Encoding encoding;

		private boolean bigEndian = true;

		private float frameRate = AudioSystem.NOT_SPECIFIED;
		private float sampleRate = AudioSystem.NOT_SPECIFIED;

		private int channels = 2; // STEREO
		private int frameSize = AudioSystem.NOT_SPECIFIED;
		private int sampleSize = AudioSystem.NOT_SPECIFIED;

		private final Map<String, Object> properties = new HashMap<>();

		private EncodedAudioFormatBuilder(Audio audio, AudioFormat.Encoding encoding) {
			Assert.notNull(encoding, "AudioFormat Encoding is required");
			this.audio = AudioUtils.assertAudio(audio);
			this.encoding = encoding;
		}

		EncodedAudioFormatBuilder inBigEndian() {
			this.bigEndian = true;
			return this;
		}

		EncodedAudioFormatBuilder inLittleEndian() {
			this.bigEndian = false;
			return this;
		}

		EncodedAudioFormatBuilder inMono() {
			this.channels = 1;
			return this;
		}

		EncodedAudioFormatBuilder inStereo() {
			this.channels = 2;
			return this;
		}

		EncodedAudioFormatBuilder withFrameRate(float frameRate) {
			this.frameRate = frameRate;
			return this;
		}

		EncodedAudioFormatBuilder withFrameSize(int frameSize) {
			this.frameSize = frameSize;
			return this;
		}

		EncodedAudioFormatBuilder withSampleRate(float sampleRate) {
			this.sampleRate = sampleRate;
			return this;
		}

		EncodedAudioFormatBuilder withSameSize(int sampleSize) {
			this.sampleSize = sampleSize;
			return this;
		}

		@Override
		public AudioFormat build() {

			AudioFormat encodedAudioFormat = buildAudioFormat();

			return AudioFormatBuilder.from(getAudio())
				.copy(encodedAudioFormat)
				.build();
		}

		private AudioFormat buildAudioFormat() {
			return new AudioFormat(getEncoding(), getSampleRate(), getSampleSize(), getChannels(),
				getFrameSize(), getFrameRate(), isBigEndian(), getProperties());
		}
	}
}
