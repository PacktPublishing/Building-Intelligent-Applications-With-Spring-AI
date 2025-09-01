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

import java.net.URL;
import java.util.UUID;

import io.packt.spring.ai.examples.app.chat.model.AudioMessage;
import io.packt.spring.ai.examples.app.chat.model.ChatMessage;
import io.packt.spring.ai.examples.app.chat.model.ChatSession;
import io.packt.spring.ai.examples.app.chat.model.ChatUser;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;

/**
 * Service interface defining chat operations.
 *
 * @author John Blum
 * @since 0.1.0
 */
public interface ChatService {

	ChatSession findBy(UUID sessionId);

	ChatSession joinChatSession(UUID sessionId, ChatUser user);

	ChatSession newChatSession(ChatUser user);

	String resolveChatSessionId(URL sessionUrl);

	URL resolveChatSessionUrl(String sessionId);

	AudioMessage textToSpeech(ChatMessage message);

	ChatMessage translateChatMessage(ChatMessage message, IsoLanguage language);

}
