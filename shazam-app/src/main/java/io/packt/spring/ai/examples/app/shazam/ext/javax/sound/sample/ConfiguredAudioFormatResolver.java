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

import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.function.FunctionUtils;

/**
 * {@link AudioFormatResolver} resolving the {@link AudioFormat} of {@link Audio}
 * configured on the given {@link Audio} object.
 *
 * @author John Blum
 * @see Audio
 * @see javax.sound.sampled.AudioFormat
 * @since 0.1.0
 */
public class ConfiguredAudioFormatResolver implements AudioFormatResolver {

	public static final ConfiguredAudioFormatResolver INSTANCE = new ConfiguredAudioFormatResolver();

	@Override
	public AudioFormat resolve(Audio audio, Supplier<AudioFormat> defaultAudioFormat) {
		return ExceptionThrowingSupplier.getSafely(audio::getFormat, cause ->
			FunctionUtils.nullSafeSupplier(defaultAudioFormat).get());
	}
}
