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
package io.codeprimate.tools.spring.ai.tokens;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} for the Token Cost Estimator.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@Slf4j
@SpringBootApplication
@Getter(AccessLevel.PROTECTED)
@Profile(TokenCostEstimatorApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class TokenCostEstimatorApplication {

	public static final String SPRING_APPLICATION_PROFILE = "token-cost-estimator-app";
	private static final String WEBAPP_URI = "http://localhost:%d/%s";

	private static final boolean DEFAULT_HEADLESS = false;

	public static void main(String[] args) {

		new SpringApplicationBuilder(TokenCostEstimatorApplication.class)
			.web(WebApplicationType.SERVLET)
			.profiles(SPRING_APPLICATION_PROFILE)
			.headless(isHeadless())
			.build()
			.run(args);
	}

	private static boolean isHeadless() {
		return Boolean.getBoolean("app.token-cost-estimator.headless") || DEFAULT_HEADLESS;
	}

	@Bean
	ApplicationRunner programRunner(@Value("${spring.application.name}") String applicationName,
			@Value("${server.servlet.contextPath}") String servletContextPath,
			@Value("${server.port:8080}") int serverPort) {

		return args -> {

			log.info("Welcome to {}", applicationName);

			URI webappUri = URI.create(WEBAPP_URI.formatted(serverPort, servletContextPath));

			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(webappUri);
			}
			else {
				log.warn("Unable to launch {}; Open a web browser to [{}]", applicationName, webappUri);
			}
		};
	}
}
