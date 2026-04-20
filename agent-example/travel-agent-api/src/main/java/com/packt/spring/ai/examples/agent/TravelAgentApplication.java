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
package com.packt.spring.ai.examples.agent;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * {@link SpringBootApplication} for an Agentic AI System implementing a Travel Agent.
 *
 * @author John Blum
 * @see SpringBootApplication
 * @see ApplicationRunner
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(TravelAgentApplication.TRAVEL_AGENT_PROFILE)
public class TravelAgentApplication extends AbstractSpringBootApplication {

	public static final String TRAVEL_AGENT_PROFILE = "travel-agent";

	public static void main(String[] args) {
		runSpringApplication(TravelAgentApplication.class, asStringArray(TRAVEL_AGENT_PROFILE), args);
	}

	@Bean
	ApplicationRunner programRunner() {

		return arguments -> {
			getLogger().info("Example Spring Boot, Spring AI Travel Agent application");
			getLogger().info("Open OpenAI ChatGPT, Claude Desktop or similar AI app to access the Spring Boot,"
				+ " Spring AI Travel Agent application as an MCP Server");
		};
	}
}
