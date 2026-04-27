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

import org.cp.elements.lang.Assert;
import org.springframework.lang.Nullable;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of {@literal sort criteria}.
 *
 * @author John Blum
 * @see Enum
 * @since 0.1.0
 */
@Getter
public enum SortBy {

	TOP_FLIGHTS(1),
	PRICE(2),
	DEPARTURE_TIME(3),
	ARRIVAL_TIME(4),
	DURATION(5),
	EMISSION(6);

	public static final SortBy DEFAULT = PRICE;

	public static SortBy defaultIfNull(@Nullable SortBy sort) {
		return sort != null ? sort : DEFAULT;
	}

	private final int option;

	SortBy(int option) {
		Assert.isTrue(option > 0, "Option must be greater than 0");
		this.option = option;
	}

	public String getOptionAsString() {
		return String.valueOf(getOption());
	}
}
