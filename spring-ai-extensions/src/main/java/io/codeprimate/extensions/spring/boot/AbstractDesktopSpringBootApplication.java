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
package io.codeprimate.extensions.spring.boot;

import java.awt.Desktop;
import java.net.URI;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Abstract base class for desktop-based, Spring Boot Web applications.
 *
 * @author John Blum
 * @see java.awt.Desktop
 * @see java.net.URI
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractDesktopSpringBootApplication extends AbstractSpringBootApplication {

	protected static final boolean HEADLESS = true;
	protected static final boolean NOT_HEADLESS = false;

	protected static final int PORT = 80;
	protected static final int SECURE_PORT = 443;

	private static final Function<SpringApplicationBuilder, SpringApplicationBuilder> DESKTOP_APPLICATION_FUNCTION =
		springApplicationBuilder -> springApplicationBuilder.headless(NOT_HEADLESS);

	private static final String JAVA_AWT_HEADLESS_SYSTEM_PROPERTY = "java.awt.headless";
	private static final String SERVER_PORT_PROPERTY = "server.port";
	private static final String SERVER_SSL_PORT_PROPERTY = "server.ssl.port";
	private static final String WEBAPP_URL = "http://localhost:%d%s";
	private static final String SECURE_WEBAPP_URL = "https://localhost:%d%s";
	private static final String UNDEFINED = "UNDEFINED";

	protected static SpringApplication runSpringReactiveWebApplication(Class<?> mainApplicationClass, String[] profiles,
			String... args) {

		return runSpringReactiveWebApplication(mainApplicationClass, profiles, Function.identity(), args);
	}

	protected static SpringApplication runSpringReactiveWebApplication(Class<?> mainApplicationClass, String[] profiles,
			Function<SpringApplicationBuilder, SpringApplicationBuilder> function, String... args) {

		return runSpringApplication(mainApplicationClass, profiles,
			REACTIVE_WEB_APPLICATION_FUNCTION.andThen(DESKTOP_APPLICATION_FUNCTION).andThen(function), args);
	}

	protected static SpringApplication runSpringServletWebApplication(Class<?> mainApplicationClass, String[] profiles,
			String... args) {

		return runSpringServletWebApplication(mainApplicationClass, profiles, Function.identity(), args);
	}

	protected static SpringApplication runSpringServletWebApplication(Class<?> mainApplicationClass, String[] profiles,
			Function<SpringApplicationBuilder, SpringApplicationBuilder> function, String... args) {

		return runSpringApplication(mainApplicationClass, profiles,
			SERVLET_WEB_APPLICATION_FUNCTION.andThen(DESKTOP_APPLICATION_FUNCTION).andThen(function), args);
	}

	@Bean
	@ConditionalOnWebApplication
	ApplicationRunner launchWebApplication(Environment environment,
			@Value("${spring.application.name:UNDEFINED}") String applicationName,
			@Value("${server.servlet.contextPath:/}") String serverServletContextPath,
			@Value("${server.port:8080}") int serverPort) {

		return args -> {

			getLogger().info("Welcome to %s".formatted(applicationName));

			String webApplicationUrl =
				getWebApplicationUrl(environment).formatted(serverPort, serverServletContextPath);

			URI webAppliactionUri = URI.create(webApplicationUrl);

			if (isWebApplicationLaunchEnabled()) {
				Desktop.getDesktop().browse(webAppliactionUri);
			}
			else {
				getLogger().warn("Unable to launch {} Web application", applicationName);
				getLogger().warn("Is Desktop enabled?");
				getLogger().warn("Open web browser to {}", webAppliactionUri);
			}
		};
	}

	private boolean isDesktopEnabled() {
		return Desktop.isDesktopSupported();
	}

	private boolean isHeadless() {
		return Boolean.getBoolean(JAVA_AWT_HEADLESS_SYSTEM_PROPERTY);
	}

	private boolean isNotHeadless() {
		return !isHeadless();
	}

	private boolean isSecureWebApplication(Environment environment) {

		String securePort = String.valueOf(SECURE_PORT);
		String serverPort = environment.getProperty(SERVER_PORT_PROPERTY, UNDEFINED);
		String serverSslPort = environment.getProperty(SERVER_SSL_PORT_PROPERTY, UNDEFINED);

		return serverPort.contains(securePort) || serverSslPort.contains(securePort);
	}

	private boolean isWebApplicationLaunchEnabled() {
		return isDesktopEnabled() && isNotHeadless();
	}

	protected String getWebApplicationUrl(Environment environment) {
		return isSecureWebApplication(environment) ? SECURE_WEBAPP_URL : WEBAPP_URL;
	}
}
