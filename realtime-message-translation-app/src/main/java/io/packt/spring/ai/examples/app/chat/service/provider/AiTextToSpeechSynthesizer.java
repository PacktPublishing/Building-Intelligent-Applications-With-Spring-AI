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
package io.packt.spring.ai.examples.app.chat.service.provider;

import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.TextToSpeechSynthesizer;

import org.springframework.ai.audio.tts.TextToSpeechMessage;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} implementing {@link TextToSpeechSynthesizer} to synthesis speech (audio signals)
 * from {@link String written text}.
 *
 * @author John Blum
 * @see Service
 * @see TextToSpeechSynthesizer
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class AiTextToSpeechSynthesizer implements TextToSpeechSynthesizer {

	private static final String OPENAI_TTS_MODEL = "gpt-4o-mini-tts";

	private final TextToSpeechModel model;

	@Override
	public AudioMessage speak(TextMessage textMessage) {

		TextToSpeechMessage message = newTextToSpeechMessage(textMessage);
		TextToSpeechPrompt prompt = newSpeechPrompt(message);
		TextToSpeechResponse response = getModel().call(prompt);

		byte[] audioData = response.getResult().getOutput();

		return AudioMessage.from(audioData);
	}

	private TextToSpeechMessage newTextToSpeechMessage(TextMessage message) {
		return new TextToSpeechMessage(message.getText());
	}

	private TextToSpeechPrompt newSpeechPrompt(TextToSpeechMessage message) {
		return new TextToSpeechPrompt(message, newTextToSpeechOptions());
	}

	private TextToSpeechOptions newTextToSpeechOptions() {

		return TextToSpeechOptions.builder()
			.voice(OpenAiAudioSpeechOptions.Voice.ALLOY.toString())
			.format(OpenAiAudioSpeechOptions.AudioResponseFormat.MP3.toString())
			.model(OPENAI_TTS_MODEL)
			.build();
	}
}
