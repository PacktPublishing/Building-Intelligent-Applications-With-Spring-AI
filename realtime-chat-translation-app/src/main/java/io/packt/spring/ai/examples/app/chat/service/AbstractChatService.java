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

import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import io.packt.spring.ai.examples.app.chat.util.NetworkUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract base class encapsulating operations common to all {@link ChatService} implementations.
 *
 * @author John Blum
 * @see ChatService
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public abstract class AbstractChatService implements ChatService {

	protected static final String BASE_CHAT_SESSION_URI = "http://%s:%d%s/sessions/%s";

	@Value("${server.servlet.contextPath}")
	private String applicationContextPath;

	@Value("${server.port}")
	private int serverPort;

	protected String assertChatSessionId(String chatSessionId) {
		Assert.hasText(chatSessionId, () -> "Chat Session ID [%s] is required".formatted(chatSessionId));
		chatSessionIdValidator().accept(chatSessionId);
		return chatSessionId;
	}


	protected String assertChatSessionIdFromUri(String chatSessionId, URL chatSessionUri) {
		Assert.hasText(chatSessionId, () -> "Chat Session ID [%s] could not be resolved from URI [%s]"
			.formatted(chatSessionId, chatSessionUri));
		return assertChatSessionId(chatSessionId);
	}

	protected Consumer<String> chatSessionIdValidator() {
		return chatSessionId -> { };
	}

	protected URL assertChatSessionUrl(URL chatSessionUri) {
		Assert.notNull(chatSessionUri, "Chat Session URL is required");
		return chatSessionUri;
	}

	protected String getServerHostAddress() {
		return NetworkUtils.resolveLocalhostIpAddress();
	}

	@Override
	public String resolveChatSessionId(URL chatSessionUrl) {
		assertChatSessionUrl(chatSessionUrl);
		String chatSessionId = extractChatSessionIdFromUri(chatSessionUrl);
		assertChatSessionIdFromUri(chatSessionId, chatSessionUrl);
		return chatSessionId;
	}

	@SuppressWarnings("all")
	private String extractChatSessionIdFromUri(URL chatSessionUri) {
		String chatSessionUriAsciiString = chatSessionUri.toExternalForm();
		int index = chatSessionUriAsciiString.lastIndexOf(NetworkUtils.WEB_PATH_SEPARATOR);
		String chatSessionId = chatSessionUriAsciiString.substring(index + 1);
		return chatSessionId;
	}

	@Override
	public URL resolveChatSessionUrl(String chatSessionId) {
		assertChatSessionId(chatSessionId);
		URI chatSessionUri = resolveChatSessionUri(chatSessionId);
		return NetworkUtils.toUrl(chatSessionUri);
	}

	private URI resolveChatSessionUri(String chatSessionId) {
		return NetworkUtils.resolveUri(BASE_CHAT_SESSION_URI, getServerHostAddress(), getServerPort(),
			getApplicationContextPath(), chatSessionId);
	}
}
