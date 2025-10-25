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
package io.codeprimate.extensions.spring.ai.converter;

import com.google.json.JsonSanitizer;

import org.cp.elements.lang.Assert;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.lang.NonNull;

/**
 * Spring AI {@link StructuredOutputConverter} used to sanitize the JSON payload
 * returned in the AI model's generated response.
 *
 * @author John Blum
 * @param <T> {@link Class type} in which to map the JSON.
 * @see org.springframework.ai.converter.BeanOutputConverter
 * @see org.springframework.ai.converter.StructuredOutputConverter
 * @see <a href="https://github.com/OWASP/json-sanitizer">Google JsonSanitizer</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class JsonSanitizerOutputConverter<T> implements StructuredOutputConverter<T> {

	/**
	 * Factory method used to construct a new {@link JsonSanitizerOutputConverter} initialized with
	 * the given {@link BeanOutputConverter}.
	 *
	 * @param beanOutputConverter {@link BeanOutputConverter} used to convert JSON into a bean
	 * of the {@link Class mapped type}.
	 * @return new {@link JsonSanitizerOutputConverter}.
	 * @throws IllegalArgumentException if {@link BeanOutputConverter} is {@literal null}.
	 * @see BeanOutputConverter
	 */
	public static <T> JsonSanitizerOutputConverter<T> from(BeanOutputConverter<T> beanOutputConverter) {
		return new JsonSanitizerOutputConverter<>(beanOutputConverter);
	}

	private final BeanOutputConverter<T> beanOutputConverter;

	/**
	 * Constructs a new {@link JsonSanitizerOutputConverter} initialized with the given {@link BeanOutputConverter}.
	 *
	 * @param beanOutputConverter {@link BeanOutputConverter} used to convert JSON into a bean
	 * of the {@link Class mapped type}.
	 * @throws IllegalArgumentException if {@link BeanOutputConverter} is {@literal null}.
	 * @see BeanOutputConverter
	 */
	public JsonSanitizerOutputConverter(BeanOutputConverter<T> beanOutputConverter) {
		Assert.notNull(beanOutputConverter, "BeanOutputConverter is required");
		this.beanOutputConverter = beanOutputConverter;
	}

	/**
	 * Returns the configured {@link BeanOutputConverter} used to convert JSON into a bean
	 * of the {@link Class mapped type}.
	 *
	 * @return the configured {@link BeanOutputConverter}.
	 * @see BeanOutputConverter
	 */
	protected BeanOutputConverter<T> getBeanOutputConverter() {
		return this.beanOutputConverter;
	}

	@Override
	public String getFormat() {
		return getBeanOutputConverter().getFormat();
	}

	@Override
	public @NonNull T convert(@NonNull String json) {
		String sanitizedJson = sanitizeJson(json);
		return getBeanOutputConverter().convert(sanitizedJson);
	}

	/**
	 * Sanitizes the given {@link String JSON} into a properly formatted, structured, valid JSON.
	 *
	 * @param json {@link String JSON} to sanitize.
	 * @return sanitized JSON.
	 * @throws IllegalArgumentException if the {@link String JSON} payload is {@literal null}, empty or blank.
	 */
	protected String sanitizeJson(String json) {
		Assert.hasText(json, () -> "JSON [%s] is required".formatted(json));
		return JsonSanitizer.sanitize(json);
	}
}
