/*
 *  Copyright 2024 Author or Authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.codeprimate.extensions.spring.ai.chat.model;

import org.cp.elements.lang.Assert;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.StringUtils;

import lombok.Getter;

/**
 * Spring AI {@link ChatModel} implementation and wrapper for an existing {@link ChatModel}.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.beans.factory.BeanNameAware
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class ChatModelWrapper implements ChatModel, BeanNameAware {

	public static ChatModelWrapper from(ChatModel chatModel) {
		return new ChatModelWrapper(chatModel);
	}

	private final ChatModel chatModel;

	private volatile String beanName;

	protected ChatModelWrapper(ChatModel chatModel) {
		Assert.notNull(chatModel, "ChatModel is required");
		this.chatModel = chatModel;
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		return doAfter(getChatModel().call(doBefore(prompt)));
	}

	protected Prompt doBefore(Prompt prompt) {
		return prompt;
	}

	protected ChatResponse doAfter(ChatResponse chatResponse) {
		return chatResponse;
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return getChatModel().getDefaultOptions();
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	private String resolveName() {

		String beanName = getBeanName();

		return StringUtils.hasText(beanName) ? beanName
			: getClass().getSimpleName();
	}

	@SuppressWarnings("unchecked")
	public <T extends ChatModelWrapper> T withBeanName(String beanName) {
		setBeanName(beanName);
		return (T) this;
	}

	@Override
	public String toString() {
		return resolveName();
	}
}
