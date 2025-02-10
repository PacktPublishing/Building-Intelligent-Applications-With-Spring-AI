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

import io.codeprimate.extensions.spring.boot.AbstractDesktopSpringBootApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} providing Token Metadata such as counts and costs estimates.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractDesktopSpringBootApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Profile
 * @since 0.1.0
 */
@Slf4j
@SpringBootApplication
@Getter(AccessLevel.PROTECTED)
@Profile(TokenMetadataApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class TokenMetadataApplication extends AbstractDesktopSpringBootApplication {

	public static final String SPRING_APPLICATION_PROFILE = "token-metadata-app";

	public static void main(String[] args) {
		runSpringServletWebApplication(TokenMetadataApplication.class, asStringArray(SPRING_APPLICATION_PROFILE),
			springApplicationBuilder -> springApplicationBuilder.headless(isHeadless()), args);
	}

	private static boolean isHeadless() {
		return Boolean.getBoolean("app.token-cost-estimator.headless");
	}
}
