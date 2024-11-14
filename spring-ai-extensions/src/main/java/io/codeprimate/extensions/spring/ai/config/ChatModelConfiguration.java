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

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration} for Spring AI {@link ChatModel}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.config.ChatModelProperties
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.boot.context.properties.EnableConfigurationProperties
 * @since 0.1.0
 */
@Configuration
@EnableConfigurationProperties({ ChatModelProperties.class })
@SuppressWarnings("unused")
public class ChatModelConfiguration {

}
