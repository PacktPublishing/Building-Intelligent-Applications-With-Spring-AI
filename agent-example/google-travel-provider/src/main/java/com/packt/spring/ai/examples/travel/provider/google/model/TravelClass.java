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
package com.packt.spring.ai.examples.travel.provider.google.model;

import java.util.Arrays;

import org.cp.elements.lang.Assert;
import org.springframework.lang.Nullable;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of travel classes, such as {@literal economy}.
 *
 * @author John Blum
 * @see Enum
 * @since 0.1.0
 */
@Getter
public enum TravelClass {

	ECONOMY(1),
	PREMIUM_ECONOMY(2),
	BUSINESS(3),
	FIRST(4);

	public static final TravelClass DEFAULT = ECONOMY;

	public static TravelClass defaultIfNull(@Nullable TravelClass travelClass) {
		return travelClass != null ? travelClass : DEFAULT;
	}

	public static TravelClass from(String value) {
		return Arrays.stream(values())
			.filter(enumeratedValue -> enumeratedValue.name().equalsIgnoreCase(resolveValue(value)))
			.findFirst()
			.orElse(DEFAULT);
	}

	private static String resolveValue(@Nullable String value) {
		return String.valueOf(value).replaceAll(" ", "_");
	}

	private final int option;

	TravelClass(int option) {
		Assert.isTrue(option > 0, "Options must be greater than 0");
		this.option = option;
	}

	public String getOptionAsString() {
		return String.valueOf(getOption());
	}
}
