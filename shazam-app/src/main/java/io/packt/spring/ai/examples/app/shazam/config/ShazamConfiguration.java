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
package io.packt.spring.ai.examples.app.shazam.config;

import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.boot.web.contoller.AdminController;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootConfiguration} for the Shazam application.
 *
 * @author John Blum
 * @see SpringBootConfiguration
 * @see EnableChatClient
 * @since 0.1.0
 */
@SpringBootConfiguration
@EnableChatClient
@SuppressWarnings("unused")
public class ShazamConfiguration {

	@Bean
	AdminController adminController() {
		return new AdminController();
	}
}
