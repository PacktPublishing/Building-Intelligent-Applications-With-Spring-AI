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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import io.codeprimate.extensions.util.ExceptionThrowingRunnable;
import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.ext.ffmpeg.FFProbe;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;

import org.cp.elements.lang.Assert;
import org.springframework.lang.NonNull;

/**
 * Abstract utility class used to process {@link Audio} using the {@literal javax.sound} API and {@link FFProbe}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormat
 * @see AudioFileFormat
 * @see AudioInputStream
 * @see AudioSystem
 * @see FFProbe
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AudioUtils {

	public static final int PCM_WAV_FILE_HEADER_SIZE = 44; // bytes

	public static final AudioFileFormat.Type MP3_AUDIO_FILE_FORMAT = new AudioFileFormat.Type("MP3", "mp3");
	public static final AudioFileFormat.Type WAV_AUDIO_FILE_FORMAT = AudioFileFormat.Type.WAVE;

	private static final AtomicReference<FFProbe> ffprobe = new AtomicReference<>();

	private static final Function<Audio, Long> frameLengthFunction = audio -> {
		AudioFormat audioFormat = audio.getFormat();
		long audioSize = audio.size();
		int frameSize = audioFormat.getFrameSize();
		return audioSize / frameSize;
	};

	public static @NonNull Audio assertAudio(Audio audio) {
		Assert.notNull(audio, "Audio is required");
		return audio;
	}

	public static @NonNull AudioFormat assertAudioFormat(AudioFormat audioFormat) {
		Assert.notNull(audioFormat, "AudioFormat is required");
		return audioFormat;
	}

	public static @NonNull AudioInputStream assertAudioInputStream(AudioInputStream audioInputStream) {
		Assert.notNull(audioInputStream, "AudioInputStream is required");
		return audioInputStream;
	}

	public static boolean isSpecified(int audioValue) {
		return Math.max(audioValue, AudioSystem.NOT_SPECIFIED) > 0;
	}

	public static boolean isNotSpecified(int audioValue) {
		return !isSpecified(audioValue);
	}

	public static long calculateFrameLength(Audio audio) {
		return audio.getFormat() instanceof ShazamAudioFormat shazamAudioFormat ? shazamAudioFormat.getFrameLength()
			: frameLengthFunction.apply(audio);
	}

	public static void close(AudioInputStream in) {
		ExceptionThrowingRunnable.runSafely(in::close, cause -> { /* ignore */ });
	}

	public static AudioInputStream openInputStream(Audio audio) {
		return ExceptionThrowingSupplier.getSafely(() -> AudioSystem.getAudioInputStream(audio.inputStream()), cause -> {
			throw AudioAccessException.because("Failed to open InputStream to Audio", cause);
		});
	}

	public static FFProbe.Format probeFormat(Audio audio) {
		return ffprobe.updateAndGet(it -> FFProbe.nullSafe(it, AudioUtils::buildFFProbe))
			.showFormat(audio);
	}

	private static FFProbe buildFFProbe() {
		return FFProbe.builder()
			.enableDownload()
			.build();
	}

	@SuppressWarnings("unused")
	public static int unspecified() {
		return AudioSystem.NOT_SPECIFIED;
	}
}
