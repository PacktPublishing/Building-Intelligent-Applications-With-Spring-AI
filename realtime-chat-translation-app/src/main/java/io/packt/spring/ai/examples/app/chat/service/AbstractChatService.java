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
import java.util.UUID;
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

	protected static final String BASE_CHAT_SESSION_URI = "http://%s:%d%s/view/chat/join/%s";

	@Value("${server.servlet.contextPath}")
	private String applicationContextPath;

	@Value("${server.port:"+NetworkUtils.DEFAULT_SERVER_PORT+"}")
	private int serverPort;

	protected UUID assertChatSessionId(UUID id) {
		Assert.notNull(id, "Chat Session ID is required");
		chatSessionIdValidator().accept(id);
		return id;
	}

	protected String assertChatSessionIdFromUrl(String chatSessionId, URL chatSessionUrl) {
		Assert.hasText(chatSessionId, () -> "Chat Session ID [%s] could not be resolved from URL [%s]"
			.formatted(chatSessionId, chatSessionUrl));
		return chatSessionId;
	}

	protected URL assertChatSessionUrl(URL url) {
		Assert.notNull(url, "Chat Session URL is required");
		chatSessionUrlValidator().accept(url);
		return url;
	}

	protected Consumer<UUID> chatSessionIdValidator() {
		return chatSessionId -> { };
	}

	protected Consumer<URL> chatSessionUrlValidator() {
		return chatSessoinUrl -> { };
	}

	protected String getServerHostAddress() {
		return NetworkUtils.resolveLocalhostIpAddress();
	}

	@Override
	public UUID resolveChatSessionId(URL sessionUrl) {
		assertChatSessionUrl(sessionUrl);
		String chatSessionId = resolveChatSessionIdFromUrl(sessionUrl);
		assertChatSessionIdFromUrl(chatSessionId, sessionUrl);
		return UUID.fromString(chatSessionId);
	}

	@SuppressWarnings("all")
	private String resolveChatSessionIdFromUrl(URL url) {
		String urlExternalForm = url.toExternalForm();
		int index = urlExternalForm.lastIndexOf(NetworkUtils.WEB_PATH_SEPARATOR);
		String chatSessionId = urlExternalForm.substring(index + 1);
		return chatSessionId;
	}

	@Override
	public URL resolveChatSessionUrl(UUID sessionId) {
		assertChatSessionId(sessionId);
		URI chatSessionUri = resolveChatSessionUri(sessionId);
		return NetworkUtils.toUrl(chatSessionUri);
	}

	private URI resolveChatSessionUri(UUID chatSessionId) {
		return NetworkUtils.resolveUri(BASE_CHAT_SESSION_URI, getServerHostAddress(), getServerPort(),
			getApplicationContextPath(), chatSessionId);
	}
}
