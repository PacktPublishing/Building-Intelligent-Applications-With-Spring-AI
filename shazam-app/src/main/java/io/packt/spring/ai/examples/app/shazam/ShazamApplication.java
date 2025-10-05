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
package io.packt.spring.ai.examples.app.shazam;

import io.codeprimate.extensions.spring.boot.AbstractDesktopSpringBootApplication;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * {@link SpringBootApplication} implementing the {@literal Shazam} app called {@literal What's My Jam}.
 *
 * @author John Blum
 * @see SpringBootApplication
 * @see AbstractDesktopSpringBootApplication
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ShazamApplication extends AbstractDesktopSpringBootApplication {

	private static final String SHAZAM_APP_PROFILE = "shazam-app";
	private static final String WEBAPP_URI = "/view/recorder";

	public static void main(String[] args) {
		runSpringServletWebApplication(ShazamApplication.class, asStringArray(SHAZAM_APP_PROFILE), args);
	}

	@Bean
	ApplicationRunner programRunner(Environment environment) {
		return applicationArguments -> getLogger().info("GOTO URL [{}]", getWebApplicationUrl(environment));
	}

	@Override
	protected String getWebApplicationUrl(Environment environment) {
		return super.getWebApplicationUrl(environment).concat(WEBAPP_URI);
	}
}
