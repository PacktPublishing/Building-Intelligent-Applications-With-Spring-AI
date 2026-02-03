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

import java.io.InputStream;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.NonNull;

/**
 * Interface defining a contract for the source of an {@link AudioInputStream}.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @see java.io.InputStream
 * @see java.util.function.Supplier
 * @see javax.sound.sampled.AudioInputStream
 * @see org.springframework.core.io.InputStreamSource
 * @since 1.0.0
 */
@FunctionalInterface
public interface AudioInputStreamSource extends InputStreamSource, Supplier<AudioInputStream> {

	static AudioInputStreamSource from(AudioInputStream audioInputStream) {
		AudioUtils.assertAudioInputStream(audioInputStream);
		return () -> audioInputStream;
	}

	default AudioFormat getAudioFormat() {
		return get().getFormat();
	}

	default long getFrameLength() {
		return get().getFrameLength();
	}

	@Override
	default @NonNull InputStream getInputStream() {
		return get();
	}
}
