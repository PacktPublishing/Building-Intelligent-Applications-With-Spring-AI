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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.Constants;
import org.cp.elements.security.model.User;
import org.springframework.ai.converter.BeanOutputConverter;

/**
 * Integration Tests for {@link JsonSanitizerOutputConverter}.
 *
 * @author John Blum
 * @see JsonSanitizerOutputConverter
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class JsonSanitizerConverterIntegrationTests {

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void convertJson() {

		String JSON = "{ \"name\": \"jonDoe\" }";

		JsonSanitizerOutputConverter<TestUser> converter =
			JsonSanitizerOutputConverter.from(new BeanOutputConverter<>(TestUser.class));

		User jonDoe = converter.convert(JSON);

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("jonDoe");
	}

	@Test
	@SuppressWarnings("rawtypes")
	void sanitizeInvalidJson() {

		BeanOutputConverter<User> mockConverter = new BeanOutputConverter<>(User.class);
		JsonSanitizerOutputConverter<User> converter = JsonSanitizerOutputConverter.from(mockConverter);

		String JSON = "{name: 'jonDoe'";
		String sanitizedJson = converter.sanitizeJson(JSON);

		assertThat(sanitizedJson).isNotBlank();
		assertThat(sanitizedJson).isEqualTo("{\"name\": \"jonDoe\"}");
	}

	record TestUser(String name) implements User<UUID> {

		@Override
		public UUID getId() {
			throw new UnsupportedOperationException(Constants.NOT_IMPLEMENTED);
		}

		@Override
		public String getName() {
			return name();
		}
	}
}
