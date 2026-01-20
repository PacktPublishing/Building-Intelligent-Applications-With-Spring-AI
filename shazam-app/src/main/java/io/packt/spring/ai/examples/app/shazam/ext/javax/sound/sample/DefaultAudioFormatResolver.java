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
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;

import org.cp.elements.function.FunctionUtils;
import org.cp.elements.lang.Assert;

/**
 * Default implementation of {@link AudioFormatResolver}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFormatResolver
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
public class DefaultAudioFormatResolver implements AudioFormatResolver {

	@Override
	public AudioFormat resolve(Audio audio, Supplier<AudioFormat> defaultAudioFormat) {

		Assert.notNull(audio, "Audio is required");

		return ExceptionThrowingSupplier.getSafely(audio::getFormat, cause -> {

			AudioFormat audioFormat = null;

			try (AudioInputStream in = AudioUtils.openInputStream(audio)) {
				audioFormat = in.getFormat();
			}
			catch (AudioAccessException e) {
				audioFormat = FunctionUtils.nullSafeSupplier(defaultAudioFormat).get();
				if (audioFormat == null) {
					throw AudioAccessException.because("Failed to resolve AudioFormat of Audio", e);
				}
			}
			catch (IOException ignore) {
				// IOException thrown on AudioInputStream.close(); ignore
			}

			return audioFormat;
		});
	}
}
