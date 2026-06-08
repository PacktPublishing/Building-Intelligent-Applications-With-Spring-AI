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

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Spring AI {@link ChatModel} implementation that results in no op.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.model.ChatModel
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class NoOpChatModel implements ChatModel {

	@Override
	@SuppressWarnings("all")
	public ChatResponse call(Prompt prompt) {
		throw new UnsupportedOperationException("ChatModel.call(Prompt) is not supported");
	}
}
