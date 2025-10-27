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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Constants;
import org.cp.elements.security.model.User;
import org.springframework.ai.converter.StructuredOutputConverter;

/**
 * Unit Tests for {@link JsonSanitizerOutputConverter}.
 *
 * @author John Blum
 * @see JsonSanitizerOutputConverter
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @since 0.1.0
 */
@SuppressWarnings("unchecked")
public class JsonSanitizerOutputConverterUnitTests {

	@Test
	void constructsNewJsonSanitizerOutputConverter() {

		StructuredOutputConverter<Object> mockConverter = mock(StructuredOutputConverter.class);

		JsonSanitizerOutputConverter<Object> converter = new JsonSanitizerOutputConverter<>(mockConverter);

		assertThat(converter).isNotNull();
		assertThat(converter.getConverter()).isSameAs(mockConverter);
	}

	@Test
	void constructsNewJsonSanitizerOutputConverterWithNullBeanOutputConverter() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> new JsonSanitizerOutputConverter<>(null))
			.withMessage("StructuredOutputConverter is required")
			.withNoCause();
	}

	@Test
	void fromBeanOutputConverter() {

		StructuredOutputConverter<Object> mockConverter = mock(StructuredOutputConverter.class);

		JsonSanitizerOutputConverter<Object> converter = JsonSanitizerOutputConverter.from(mockConverter);

		assertThat(converter).isNotNull();
		assertThat(converter.getConverter()).isSameAs(mockConverter);
	}

	@Test
	void convertsJsonToBean() {

		String JSON = "{ \"name\": \"jonDoe\" }";

		StructuredOutputConverter<Object> mockConverter = mock(StructuredOutputConverter.class);

		doReturn(TestUser.named("jonDoe")).when(mockConverter).convert(anyString());

		JsonSanitizerOutputConverter<Object> converter = spy(JsonSanitizerOutputConverter.from(mockConverter));

		doReturn(JSON).when(converter).sanitizeJson(anyString());

		assertThat(converter.convert(JSON)).isEqualTo(TestUser.named("jonDoe"));

		verify(converter, times(1)).convert(eq(JSON));
		verify(converter, times(1)).sanitizeJson(eq(JSON));
		verify(converter, times(1)).getConverter();
		verify(mockConverter, times(1)).convert(eq(JSON));
		verifyNoMoreInteractions(converter, mockConverter);
	}

	@Test
	void getFormat() {

		StructuredOutputConverter<Object> mockConverter = mock(StructuredOutputConverter.class);

		doReturn("Test Format").when(mockConverter).getFormat();

		JsonSanitizerOutputConverter<Object> converter = spy(JsonSanitizerOutputConverter.from(mockConverter));

		assertThat(converter.getFormat()).isEqualTo("Test Format");

		verify(mockConverter, times(1)).getFormat();
		verifyNoMoreInteractions(mockConverter);
	}

	@Test
	void sanitizeIllegalJson() {

		StructuredOutputConverter<Object> mockConverter = mock(StructuredOutputConverter.class);
		JsonSanitizerOutputConverter<Object> converter = spy(JsonSanitizerOutputConverter.from(mockConverter));

		Arrays.asList("  ", "", null).forEach(json ->
			assertThatIllegalArgumentException()
				.isThrownBy(() -> converter.sanitizeJson(json))
				.withMessage("JSON [%s] is required", json)
				.withNoCause());

		verifyNoInteractions(mockConverter);
	}

	record TestUser(String name) implements User<UUID> {

		TestUser {
			Assert.hasText(name, "Name is required");
		}

		static TestUser named(String name) {
			return new TestUser(name);
		}

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
