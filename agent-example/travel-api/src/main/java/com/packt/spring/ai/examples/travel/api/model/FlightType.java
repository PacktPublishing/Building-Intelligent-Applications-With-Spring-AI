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
package com.packt.spring.ai.examples.travel.api.model;

import java.util.Arrays;

import org.springframework.lang.Nullable;

/**
 * {@link Enum Enumeration} of flight types, such as {@literal round trip}.
 *
 * @author John Blum
 * @see Enum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum FlightType {

	MULTI_CITY, ONE_WAY, ROUND_TRIP;

	public static final FlightType DEFAULT = ROUND_TRIP;

	public static FlightType defaultIfNull(@Nullable FlightType flightType) {
		return flightType != null ? flightType : DEFAULT;
	}

	public static FlightType from(String value) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(resolveValue(value)))
			.findFirst()
			.orElse(DEFAULT);
	}

	private static String resolveValue(@Nullable String value) {
		return String.valueOf(value).replaceAll(" ", "_");
	}
}
