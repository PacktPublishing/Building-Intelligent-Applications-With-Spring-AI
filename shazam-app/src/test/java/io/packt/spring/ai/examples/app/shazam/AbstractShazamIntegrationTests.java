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
package io.packt.spring.ai.examples.app.shazam;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Abstract base class for all Shazam Integration Tests
 *
 * @author John Blum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractShazamIntegrationTests {

	private static final boolean DEBUG = false;

	protected boolean isDebug() {
		return DEBUG;
	}

	protected void log(String message, Object... args) {
		System.out.printf(message, args);
		System.out.flush();
	}

	protected void logAudioFormat(AudioFormat audioFormat) {

		if (isDebug()) {
			log("AudioFormat [%s]%n", audioFormat);
			log("AudioFormat - Channels [%s]%n", audioFormat.getChannels());
			log("AudioFormat - Encoding [%s]%n", audioFormat.getEncoding());
			log("AudioFormat - FrameRate [%s]%n", audioFormat.getFrameRate());
			log("AudioFormat - FrameSize [%s]%n", audioFormat.getFrameSize());
			log("AudioFormat - SampleRate [%s]%n", audioFormat.getSampleRate());
			log("AudioFormat - SampleSizeInBits [%s]%n", audioFormat.getSampleSizeInBits());
		}
	}

	protected void logAudioFileFormat(AudioFileFormat audioFileFormat) {

		if (isDebug()) {
			log("AudioFileFormat [%s]%n", audioFileFormat);
			log("AudioFileFormat - ByteLength [%s]%n", audioFileFormat.getByteLength());
			log("AudioFileFormat - FrameLength [%s]%n", audioFileFormat.getFrameLength());
			log("AudioFileFormat - Type [%s]%n", audioFileFormat.getType());
			logAudioFormat(audioFileFormat.getFormat());
		}
	}

	protected Resource resource() {
		return resource(resourcePath());
	}

	protected Resource resource(String resourcePath) {
		return new ClassPathResource(resourcePath);
	}

	protected boolean resourceExists() {
		return resource().exists();
	}

	protected abstract String resourcePath();

}
