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
package io.packt.spring.ai.examples.app.shazam.service.provider;

import static io.packt.spring.ai.examples.app.shazam.support.NumberUtils.asInt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.packt.spring.ai.examples.app.shazam.config.AudioProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioInputStreamBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.service.AbstractAudioSplitter;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;
import io.packt.spring.ai.examples.app.shazam.support.TimeUtils;

import org.springframework.ai.document.Document;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link AudioSplitter} implementation using the Java Sound API.
 *
 * @author John Blum
 * @see AbstractAudioSplitter
 * @see Audio
 * @see AudioProperties
 * @see java.time.Duration
 * @see javax.sound.sampled.AudioFileFormat
 * @see javax.sound.sampled.AudioFormat
 * @see javax.sound.sampled.AudioInputStream
 * @see javax.sound.sampled.AudioSystem
 * @see org.springframework.ai.document.Document
 * @see <a href="https://www.oracle.com/java/technologies/java-sound-api.html">Java Sound API</a>
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class JavaSoundAudioSplitter extends AbstractAudioSplitter {

	protected static final AudioFileFormat.Type AUDIO_FILE_TYPE = AudioUtils.WAV_AUDIO_FILE_FORMAT;

	public JavaSoundAudioSplitter(AudioProperties audioProperties) {
		super(audioProperties);
	}

	@Override
	public List<Document> split(Audio audio) {

		AudioUtils.assertAudio(audio);

		try (AudioInputStream in = openInputStream(audio)) {

			AudioFormat audioFormat = in.getFormat();
			Duration audioClipDuration = getAudioProperties().getClipDuration();
			List<Document> documents = new ArrayList<>();

			long frameLength = computeFrameLength(audioFormat, audioClipDuration);
			long timestamp = 0L;
			int byteOffset = 0;

			while (in.available() > 0) {
				try (AudioInputStream audioClipIn = openInputStream(in, audioFormat, frameLength)) {
					ByteArrayOutputStream out = new ByteArrayOutputStream(audioClipIn.available());
					AudioSystem.write(audioClipIn, AUDIO_FILE_TYPE, out);
					byte[] audioData = out.toByteArray();
					AudioClip audioClip = AudioClip.from(audioData, audioFormat)
						.atTimestamp(timestamp)
						.fromByteOffset(byteOffset);
					Document audioDocument = buildDocument(audioClip);
					documents.add(audioDocument);
					byteOffset = computeByteOffset(byteOffset, audioData);
					timestamp = computeTimestamp(timestamp, audioClipDuration);
				}
			}

			return documents;
		}
		catch (Exception cause) {
			throw AudioAccessException.because("Failed to read audio", cause);
		}
	}

	private AudioInputStream openInputStream(Audio audio) {
		return AudioInputStreamBuilder.from(audio).build();
	}

	private AudioInputStream openInputStream(AudioInputStream in, AudioFormat audioFormat, long frameLength) {
		return new AudioInputStream(in, audioFormat, frameLength);
	}

	private int computeByteOffset(int byteOffset, byte[] audioData) throws IOException, UnsupportedAudioFileException {
		AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(audioData));
		int frameSize = audioFileFormat.getFormat().getFrameSize();
		int frameLength = audioFileFormat.getFrameLength();
		int byteLength = frameLength * frameSize;
		return byteOffset + byteLength;
	}

	// frame length is the total number of frames in the audio
	private long computeFrameLength(AudioFormat audioFormat, Duration audioClipDuration) {
		int frameRate = asInt(audioFormat.getFrameRate()); // number of frames / second
		double audioClipDurationInSeconds = (double) audioClipDuration.toMillis() / TimeUtils.MILLISECONDS_PER_SECOND;
		return Math.round(frameRate * audioClipDurationInSeconds);
	}

	private long computeTimestamp(long timestamp, Duration audioClipDuration) {
		return timestamp + audioClipDuration.toMillis();
	}
}
