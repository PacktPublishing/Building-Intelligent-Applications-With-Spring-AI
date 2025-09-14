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
package io.packt.spring.ai.examples.app.chat.service;

import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;

/**
 * {@link FunctionalInterface} defining a contract to synthesis speech from {@link String text}.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
public interface TextToSpeechSynthesizer {

	AudioMessage speak(TextMessage message);

}
