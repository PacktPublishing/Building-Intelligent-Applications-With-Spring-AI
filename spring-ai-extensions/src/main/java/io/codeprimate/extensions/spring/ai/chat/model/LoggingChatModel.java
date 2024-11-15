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
package io.codeprimate.extensions.spring.ai.chat.model;

import io.codeprimate.extensions.spring.ai.config.ChatModelProperties;

import org.cp.elements.lang.ObjectUtils;
import org.slf4j.event.Level;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring AI {@link ChatModel} implementation used to log AI chat model interactions.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.chat.model.ChatModelWrapper
 * @since 0.1.0
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class LoggingChatModel extends ChatModelWrapper {

	protected static final Level DEFAULT_LEVEL = ChatModelProperties.Logging.DEFAULT_LEVEL;

	public static LoggingChatModel from(ChatModel chatModel) {
		return from(chatModel, DEFAULT_LEVEL);
	}

	public static LoggingChatModel from(ChatModel chatModel, Level level) {
		return new LoggingChatModel(chatModel, level);
	}

	private final Level level;

	protected LoggingChatModel(ChatModel chatModel, Level level) {
		super(chatModel);
		this.level = ObjectUtils.requireObject(level, "Level is required");
	}

	@Override
	protected Prompt doBefore(Prompt prompt) {
		log.atLevel(getLevel()).log("PROMPT [{}]", prompt);
		return super.doBefore(prompt);
	}
}
