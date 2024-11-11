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
package io.codeprimate.extensions.spring.ai.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration Tests for {@link EnableRateLimit}
 *
 * @author John Blum
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@SuppressWarnings("unused")
public class RateLimitAnnotationConfigurationIntegrationTests {

	@Autowired
	private RateLimitConfiguration configuration;

	@BeforeEach
	public void assertConfigurationIsNotNull() {
		assertThat(this.configuration).isNotNull();
	}

	@Test
	void rateLimitConfiguration() {

		assertThat(this.configuration.getCount()).isEqualTo(12);
		assertThat(this.configuration.getDuration()).hasMinutes(2);
	}

	@SpringBootConfiguration
	@EnableRateLimit(count = 12, duration = "2m")
	static class RateLimitAnnotationTestConfiguration {

	}
}
