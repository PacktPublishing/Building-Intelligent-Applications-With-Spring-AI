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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.packt.spring.ai.examples.app.chat.config.ChatConfiguration;
import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.TextToSpeechSynthesizer;

import org.junit.jupiter.api.Test;

import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests for {@link AiTextToSpeechSynthesizer}.
 *
 * @author John Blum
 * @see AiTextToSpeechSynthesizer
 * @see org.junit.jupiter.api.Test
 * @see SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles({ "user", "text-to-speech-test" })
@SuppressWarnings("unused")
public class AiTextToSpeechSynthesizerIntegrationTests {

	@Autowired
	private TextToSpeechSynthesizer speechSynthesizer;

	@Test
	void textToSpeech() throws IOException {

		TextMessage textMessage = TextMessage.from("This is a test!");
		AudioMessage audioMessage = this.speechSynthesizer.speak(textMessage);

		assertThat(audioMessage).isNotNull();

		byte[] audioData = audioMessage.getData();

		File audioMp3 = new File(System.getProperty("user.dir"), "audio.mp3");

		audioMp3.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(audioMp3)) {
			out.write(audioData);
			out.flush();
		}

		assertThat(audioMp3).isFile();
		assertThat(audioMp3.length()).isGreaterThanOrEqualTo(audioData.length);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ChatConfiguration.class)
	@Profile("text-to-speech-test")
	static class TextToSpeechSynthesizerTestConfiguration {

		@Bean
		AiTextToSpeechSynthesizer textToSpeechSynthesizer(SpeechModel speechModel) {
			return new AiTextToSpeechSynthesizer(speechModel);
		}
	}
}
