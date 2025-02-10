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

import java.lang.annotation.Annotation;
import java.time.Duration;

import io.codeprimate.extensions.spring.ai.chat.client.advisor.RateLimitAdvisor;

import org.cp.elements.time.DurationFormatter;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Spring {@link Configuration} used to configure rate limiting for AI model interactions.
 *
 * @author John Blum
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.type.AnnotationMetadata
 * @since 0.1.0
 */
@Configuration
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class RateLimitConfiguration implements EnvironmentAware, ImportAware {

	protected static final int DEFAULT_COUNT = RateLimitAdvisor.DEFAULT_COUNT;

	protected static final Duration DEFAULT_DURATION = Duration.ofSeconds(1);

	protected static final String ONE_SECOND = "1s";
	protected static final String RATE_LIMIT_COUNT_ATTRIBUTE = "count";
	protected static final String RATE_LIMIT_COUNT_PROPERTY = "ext.spring.ai.model.rate-limit.count";
	protected static final String RATE_LIMIT_DURATION_ATTRIBUTE = "duration";
	protected static final String RATE_LIMIT_DURATION_PROPERTY = "ext.spring.ai.model.rate-limit.duration";

	private int count = DEFAULT_COUNT;

	private Duration duration = DEFAULT_DURATION;

	@Setter
	private Environment environment;

	@Override
	public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes enableRateLimitAttributes = getAnnotationAttributes(importMetadata);

			this.count = resolveRateLimitCount(enableRateLimitAttributes);
			this.duration = resolveRateLimitDuration(enableRateLimitAttributes);
		}
	}

	private Duration resolveRateLimitDuration(AnnotationAttributes enableRateLimitAttributes) {

		String duration = getEnvironment().getProperty(RATE_LIMIT_DURATION_PROPERTY, String.class,
			enableRateLimitAttributes.getString(RATE_LIMIT_DURATION_ATTRIBUTE));

		return DurationFormatter.TIME_UNIT.parse(duration);
	}

	private Integer resolveRateLimitCount(AnnotationAttributes enableRateLimitAttributes) {
		return getEnvironment().getProperty(RATE_LIMIT_COUNT_PROPERTY, Integer.class,
			enableRateLimitAttributes.getNumber(RATE_LIMIT_COUNT_ATTRIBUTE));
	}

	protected boolean isAnnotationPresent(AnnotationMetadata metadata) {
		return metadata.hasAnnotation(getAnnotationTypeName());
	}

	protected AnnotationAttributes getAnnotationAttributes(AnnotationMetadata metadata) {
		return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(getAnnotationTypeName()));
	}

	protected Class<? extends Annotation> getAnnotationType() {
		return EnableRateLimit.class;
	}

	protected String getAnnotationTypeName() {
		return getAnnotationType().getName();
	}

	@Bean
	ChatClientCustomizer rateLimitChatClientCustomizer() {

		return chatClientBuilder ->
			chatClientBuilder.defaultAdvisors(RateLimitAdvisor.countPerDuration(getCount(), getDuration()));
	}
}
