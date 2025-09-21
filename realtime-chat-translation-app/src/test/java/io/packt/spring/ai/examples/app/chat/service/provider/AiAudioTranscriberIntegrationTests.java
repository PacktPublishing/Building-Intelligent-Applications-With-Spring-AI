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

import io.packt.spring.ai.examples.app.chat.config.ChatConfiguration;
import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.AudioTranscriber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests for {@link AiAudioTranscriber}.
 *
 * @author John Blum
 * @see AiAudioTranscriber
 * @see org.junit.jupiter.api.Test
 * @see SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@Getter(AccessLevel.PROTECTED)
@ActiveProfiles({ "user", "audio-transcription-tests" })
@EnabledIfSystemProperty(named = "integration-tests", matches = "true")
@SuppressWarnings("unused")
public class AiAudioTranscriberIntegrationTests {

	@Autowired
	private AudioTranscriber audioTranscriber;

	@Test
	void transcribeAudioCorrectly() {

		Resource audio = new ClassPathResource("/audio.mp3");

		assertThat(audio).isNotNull();
		assertThat(audio.exists()).isTrue();

		AudioMessage audioMessage = AudioMessage.from(audio);
		TextMessage textMessage = getAudioTranscriber().transcribe(audioMessage);

		assertThat(textMessage).isNotNull();
		assertThat(textMessage.getText()).isNotBlank();
		assertThat(textMessage.getText().trim()).isEqualToIgnoringCase("this is a test.");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ChatConfiguration.class)
	static class AudioTranscriptionTestConfiguration {

		@Bean
		AudioTranscriber audioTranscriber(OpenAiAudioTranscriptionModel model) {
			return new AiAudioTranscriber(model);
		}
	}
}
