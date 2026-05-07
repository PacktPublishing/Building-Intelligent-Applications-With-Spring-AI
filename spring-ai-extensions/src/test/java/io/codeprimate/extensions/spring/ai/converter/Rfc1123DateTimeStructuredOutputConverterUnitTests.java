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

import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link Rfc1123DateTimeStructuredOutputConverter}.
 *
 * @author John Blum
 * @see java.time.ZonedDateTime
 * @see java.time.format.DateTimeFormatter
 * @see org.junit.jupiter.api.Test
 * @see Rfc1123DateTimeStructuredOutputConverter
 * @since 0.1.0
 */
public class Rfc1123DateTimeStructuredOutputConverterUnitTests {

	@BeforeAll
	public static void printCurrentDateTimeInLocalTimezone() {

		ZonedDateTime now = ZonedDateTime.now();

		String formattedNow = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);

		ZonedDateTime dateTime = Rfc1123DateTimeStructuredOutputConverter.INSTANCE.convert(formattedNow);

		assertThat(dateTime).isNotNull();

		String formattedDateTime = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String timezone = now.getZone().getDisplayName(TextStyle.FULL, Locale.getDefault());

		System.out.printf("The current date/time (formatted: %s) in %s is: %s%n",
			formattedNow, timezone, formattedDateTime);
	}

	private void assertDateTimeAtTimezoneOffset(String dateTimeString, int year, Month month, int dayOfMonth,
		int hour, int minute, int second, String zoneOffset) {

		ZonedDateTime dateTime = Rfc1123DateTimeStructuredOutputConverter.INSTANCE
			.convert(dateTimeString);

		assertThat(dateTime).isNotNull();
		assertThat(dateTime.getYear()).isEqualTo(year);
		assertThat(dateTime.getMonth()).isEqualTo(month);
		assertThat(dateTime.getDayOfMonth()).isEqualTo(dayOfMonth);
		assertThat(dateTime.getHour()).isEqualTo(hour);
		assertThat(dateTime.getMinute()).isEqualTo(minute);
		assertThat(dateTime.getSecond()).isEqualTo(second);
		assertThat(dateTime.getOffset().toString()).isEqualTo(zoneOffset);
	}

	@Test
	void convertDateTimeOne() {
		assertDateTimeAtTimezoneOffset("Sat, 09 Nov 2024 21:07:01 +0100", 2024, Month.NOVEMBER, 9, 21, 7, 1, "+01:00");
	}

	@Test
	void convertDateTimeTwo() {
		assertDateTimeAtTimezoneOffset("Mon, 02 Jun 2025 02:32:19 +0200", 2025, Month.JUNE, 2, 2, 32, 19, "+02:00");
	}

	@Test
	void convertDateTimeThree() {
		assertDateTimeAtTimezoneOffset("Thu, 07 May 2026 20:41:35 EDT", 2026, Month.MAY, 7, 20, 41, 35, "-04:00");
	}
}
