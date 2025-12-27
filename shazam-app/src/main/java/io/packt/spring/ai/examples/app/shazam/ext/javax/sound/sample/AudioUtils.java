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
package io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.ffmpeg.FFProbe;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

/**
 * Abstract utility class used to process {@link Audio} using the {@literal javax.sound} API and {@link FFProbe}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSystem
 * @see FFProbe
 * @since 0.1.0
 */
public abstract class AudioUtils {

	private static final AtomicReference<FFProbe> ffprobe = new AtomicReference<>();

	public static boolean isSpecified(int audioValue) {
		return Math.max(audioValue, AudioSystem.NOT_SPECIFIED) > 0;
	}

	public static boolean isNotSpecified(int audioValue) {
		return !isSpecified(audioValue);
	}

	public static AudioInputStream openInputStream(Audio audio) {
		return ExceptionThrowingSupplier.getSafely(() ->
			AudioSystem.getAudioInputStream(audio.inputStream()));
	}

	public static FFProbe.Format probeFormat(Audio audio) {
		return ffprobe.updateAndGet(AudioUtils::resolveFFProbe).showFormat(audio);
	}

	public static AudioFormat resolveAudioFormat(Audio audio) {

		AudioFormat audioFormat = null;

		try (AudioInputStream in = openInputStream(audio)) {
			audioFormat = in.getFormat();
		}
		catch (IOException ignore) {
			// IOException thrown from AudioInputStream.close(); ignore
			// Throws RuntimeException when trying to open AudioInputStream
		}

		return audioFormat;
	}

	private static FFProbe resolveFFProbe(FFProbe ffprobe) {
		return ffprobe != null ? ffprobe : buildFFProbe();
	}

	private static FFProbe buildFFProbe() {
		return FFProbe.builder()
			.enableDownload()
			.build();
	}
}
