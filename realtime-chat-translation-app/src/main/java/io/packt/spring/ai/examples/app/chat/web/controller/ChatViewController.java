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
package io.packt.spring.ai.examples.app.chat.web.controller;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.packt.spring.ai.examples.app.chat.model.ChatSession;
import io.packt.spring.ai.examples.app.chat.model.ChatUser;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguage;
import io.packt.spring.ai.examples.app.chat.model.IsoLanguages;
import io.packt.spring.ai.examples.app.chat.service.ChatService;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Spring Web MVC {@link Controller} used to return Chat UI views.
 *
 * @author John Blum
 * @see Controller
 * @see ChatService
 * @see ChatSession
 * @see ChatUser
 * @since 0.1.0
 */
@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ChatViewController {

	private final ChatService chatService;

	private final ObjectMapper objectMapper;

	@GetMapping(value = { "/chat/join", "/chat/join/{chatSessionId}" })
	public ModelAndView viewJoinChat(@PathVariable(name = "chatSessionId", required = false) String chatSessionId) {

		ModelAndView modelView = new ModelAndView("JoinChat.html");
		Map<String, Object> model = modelView.getModel();

		model.put("languages", IsoLanguages.all());

		if (StringUtils.hasText(chatSessionId)) {
			model.put("chatSessionId", chatSessionId);
			model.put("chatSessionUrl", getChatService().resolveChatSessionUrl(chatSessionId));
		}

		return modelView;
	}

	@PostMapping("/chat")
	public ModelAndView joinChat(@ModelAttribute("joinChatForm") JoinChatForm form) {

		ChatUser user = resolveChatUser(form);
		ChatSession session = resolveChatSession(form, user);

		ModelAndView modelView = new ModelAndView("Chat.html");
		Map<String, Object> model = modelView.getModel();

		model.put("chatSessionId", session.getId());
		model.put("chatSessionUrl", getChatService().resolveChatSessionUrl(resolveChatSessionId(session)));
		model.put("chatUserId", user.id());
		model.put("chatUserName", user.name());
		model.put("chatUsers", toJson(session.allUsersExcluding(user)));

		return modelView;
	}

	protected ChatUser resolveChatUser(JoinChatForm form) {

		String username = form.resolveChatUserName();
		IsoLanguage language = form.resolveChatUserLanguage();

		return ChatUser.from(username, language);
	}

	protected ChatSession resolveChatSession(JoinChatForm form, ChatUser user) {

		if (form.isChatSessionIdPresent()) {
			UUID sessionId = form.resolveChatSessionId();
			return getChatService().joinChatSession(sessionId, user);
		}
		else {
			return getChatService().newChatSession(user);
		}
	}

	protected String resolveChatSessionId(ChatSession session) {
		return session.getId().toString();
	}

	private String toJson(Object value) {
		try {
			return getObjectMapper().writeValueAsString(value);
		}
		catch (JsonProcessingException cause) {
			throw new RuntimeException(cause);
		}
	}

	@Data
	@ToString
	public static class JoinChatForm {

		private String chatSessionId;
		private String name;
		private String language;

		private URL chatSessionUrl;
		private URL url;

		boolean isChatSessionIdPresent() {
			return StringUtils.hasText(getChatSessionId());
		}

		@Nullable UUID resolveChatSessionId() {
			return isChatSessionIdPresent() ? UUID.fromString(getChatSessionId().trim()) : null;
		}

		String resolveChatUserName() {
			String chatUserName = getName();
			Assert.hasText(chatUserName, () -> "Name [%s] of user is required".formatted(chatUserName));
			return chatUserName;
		}

		IsoLanguage resolveChatUserLanguage() {
			String languageCode = getLanguage();
			Assert.hasText(languageCode, "Language [%s] spoken by user is required".formatted(languageCode));
			return IsoLanguages.all().findBy(languageCode);
		}
	}
}
