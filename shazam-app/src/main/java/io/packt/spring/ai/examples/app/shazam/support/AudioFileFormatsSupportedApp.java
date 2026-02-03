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
import javax.sound.sampled.AudioSystem;

/**
 * Java program used to print the audio file formats currently supported by the Java Sound {@link AudioSystem}.
 *
 * @author John Blum
 * @see javax.sound.sampled.AudioFileFormat
 * @see javax.sound.sampled.AudioSystem
 * @since 0.1.0
 */
public class AudioFileFormatsSupportedApp {

	public static void main(String[] args) {
		Arrays.stream(AudioSystem.getAudioFileTypes())
			.map(AudioFileFormat.Type::toString)
			.sorted()
			.forEach(System.out::println);
	}
}
