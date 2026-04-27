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
package com.packt.spring.ai.examples.travel.provider.google.config;

import static org.cp.elements.lang.ElementsExceptionsFactory.newConfigurationException;

import java.net.URI;
import java.net.URL;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link ConfigurationProperties} for {@literal SerpApi}.
 *
 * @author John Blum
 * @see ConfigurationProperties
 * @since 0.1.0
 */
@Getter
@Setter
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "travel-agent.app.provider.serpapi")
public class SerpApiProperties {

	private String apiKey;
	private String engine;

	private URL baseUrl;

	public URI getBaseUri() {
		return ExceptionThrowingSupplier.getSafely(getBaseUrl()::toURI, cause -> {
			throw newConfigurationException(cause, "Failed to resolve URI from URL [%s]", getBaseUrl());
		});
	}
}
