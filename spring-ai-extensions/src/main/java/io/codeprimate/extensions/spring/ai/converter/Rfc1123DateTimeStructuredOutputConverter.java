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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.lang.NonNull;

/**
 * Spring AI {@link StructuredOutputConverter} used to parse a {@literal RFC 1123} formatted {@link String}
 * as a {@link ZonedDateTime}
 *
 * @author John Blum
 * @see java.time.ZonedDateTime
 * @see java.time.format.DateTimeFormatter#RFC_1123_DATE_TIME
 * @see org.springframework.ai.converter.StructuredOutputConverter
 */
@SuppressWarnings("unused")
public class Rfc1123DateTimeStructuredOutputConverter implements StructuredOutputConverter<ZonedDateTime> {

	public static final Rfc1123DateTimeStructuredOutputConverter INSTANCE
		= new Rfc1123DateTimeStructuredOutputConverter();

	@Override
	public String getFormat() {
		return "Use format RFC 1123. Only respond with date, time and timezone offset.";
	}

	@Override
	public ZonedDateTime convert(@NonNull String source) {
		return ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(source));
		//return ZonedDateTime.parse(source, DateTimeFormatter.RFC_1123_DATE_TIME);
	}
}
