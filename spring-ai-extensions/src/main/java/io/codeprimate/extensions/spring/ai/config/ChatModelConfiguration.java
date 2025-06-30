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
package io.codeprimate.extensions.spring.ai.config;

import java.util.List;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.chat.model.LoggingChatModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

/**
 * Spring {@link Configuration} for Spring AI {@link ChatModel}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.config.ChatModelProperties
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.context.properties.EnableConfigurationProperties
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Configuration
@EnableConfigurationProperties({ ChatModelProperties.class })
@SuppressWarnings("unused")
public class ChatModelConfiguration {

	@Bean
	@Primary
	public CompositeChatModel compositeChatModel(List<ChatModel> chatModels) {
		return CompositeChatModel.of(chatModels);
	}

	@Bean
	public static BeanPostProcessor loggingChatModelBeanPostProcessor(ChatModelProperties chatModelProperties) {

		return new BeanPostProcessor() {

			@Override
			public @NonNull Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {

				if (bean instanceof ChatModel chatModel) {
					if (isNotCompositeChatModel(chatModel)) {
						Level logLevel = chatModelProperties.logging().resolveLevel();
						if (isLoggingEnabled(chatModel, logLevel)) {
							bean = LoggingChatModel.from(chatModel, logLevel).withBeanName(beanName);
						}
					}
				}

				return bean;
			}
		};
	}

	private static boolean isCompositeChatModel(Object target) {
		return target instanceof CompositeChatModel;
	}

	private static boolean isNotCompositeChatModel(Object target) {
		return !isCompositeChatModel(target);
	}

	private static boolean isLoggingEnabled(ChatModel chatModel, Level level) {

		Class<? extends ChatModel> loggerType = resolveLoggerType(chatModel);
		Logger chatModelLogger = LoggerFactory.getLogger(loggerType);

		return chatModelLogger.isEnabledForLevel(level);
	}

	private static Class<? extends ChatModel> resolveLoggerType(ChatModel chatModel) {
		return chatModel != null ? chatModel.getClass() : LoggingChatModel.class;
	}
}
