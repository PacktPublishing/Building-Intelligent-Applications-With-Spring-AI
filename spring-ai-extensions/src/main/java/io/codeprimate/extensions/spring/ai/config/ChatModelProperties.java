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

import java.util.Set;

import org.cp.elements.lang.Assert;
import org.slf4j.event.Level;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Role;

/**
 * Property configuration for Spring AI {@link ChatModel} extensions.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 0.1.0
 */
@ConfigurationProperties("ext.spring.ai.chat.model")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@SuppressWarnings("unused")
public record ChatModelProperties(Logging logging) {

	public ChatModelProperties {
		logging = logging != null ? logging : new Logging(Logging.DEFAULT_LEVEL);
	}

	public record Logging(Level level) {

		public static final Level DEFAULT_LEVEL = Level.INFO;

		public boolean isEnabled() {
			Level configuredLevel = level();
			return configuredLevel != null && Set.of(Level.values()).contains(configuredLevel);
		}

		public Level level(Level defaultLevel) {
			Assert.notNull(defaultLevel, "Default Level is required");
			Level configuredLevel = level();
			return configuredLevel != null ? configuredLevel : defaultLevel;
		}

		public Level resolveLevel() {
			return level(DEFAULT_LEVEL);
		}
	}
}
