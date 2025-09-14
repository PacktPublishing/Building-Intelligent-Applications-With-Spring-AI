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

import java.util.UUID;

import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.ChatMessage;
import io.packt.spring.ai.examples.app.chat.model.ChatSession;
import io.packt.spring.ai.examples.app.chat.model.ChatSessions;
import io.packt.spring.ai.examples.app.chat.model.ChatUser;
import io.packt.spring.ai.examples.app.chat.model.ChatUsers;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.AbstractChatService;
import io.packt.spring.ai.examples.app.chat.service.AudioTranscriber;
import io.packt.spring.ai.examples.app.chat.service.ChatService;
import io.packt.spring.ai.examples.app.chat.service.LanguageTranslator;
import io.packt.spring.ai.examples.app.chat.service.TextToSpeechSynthesizer;
import io.packt.spring.ai.examples.app.chat.util.InvalidChatSessionException;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} implementing {@link ChatService} managing chat sessions in-memory
 * and using AI for real-time chat translation.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.ChatClient
 * @see AbstractChatService
 * @see ChatUsers
 * @see Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class SmartChatService extends AbstractChatService {

	private final AudioTranscriber audioTranscriber;

	private final ChatSessions chatSessions = ChatSessions.empty().mutable();

	private final LanguageTranslator languageTranslator;

	private final TextToSpeechSynthesizer speechSynthesizer;

	@Override
	public ChatSession findBy(UUID sessionId) {
		return getChatSessions().findBy(sessionId);
	}

	@Override
	public ChatSession joinChatSession(UUID sessionId, ChatUser user) {

		ChatSession chatSession = findBy(sessionId);

		if (!chatSession.add(user)) {
			throw InvalidChatSessionException.from(chatSession, user);
		}

		return chatSession;
	}

	@Override
	public ChatSession newChatSession(ChatUser user) {
		ChatSession session = ChatSession.withUser(user);
		getChatSessions().add(session);
		return session;
	}

	@Override
	public AudioMessage textToSpeech(TextMessage message) {

		Assert.notNull(message, "TextMessage is required");

		return getSpeechSynthesizer().speak(message);
	}

	@Override
	public TextMessage transcribeAudio(AudioMessage message) {

		Assert.notNull(message, "AudioMessage is required");

		return getAudioTranscriber().transcribe(message);
	}

	@Override
	public ChatMessage translateChatMessage(ChatMessage message, IsoLanguage language) {

		Assert.notNull(message, "ChatMessage is required");
		Assert.notNull(language, "Language is required");

		IsoLanguage messageLanguage = message.language();

		if (messageLanguage.isNotEqualTo(language)) {
			return message.findBy(language).orElseGet(() -> {
				String originalMessage = message.message();
				String translatedMessage = getLanguageTranslator().translate(originalMessage, messageLanguage, language);
				ChatMessage translatedChatMessage =
					ChatMessage.from(message).with(translatedMessage).in(language).build();
				message.add(translatedChatMessage);
				return translatedChatMessage;
			});
		}

		return message;
	}
}
