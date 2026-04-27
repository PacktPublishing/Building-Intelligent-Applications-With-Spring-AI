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

import lombok.Getter;

/**
 * {@link Enum Enumeration} of stops
 *
 * @author John Blum
 * @see Enum
 * @since 0.1.0
 */
@Getter
public enum Stops {

	UNLIMITED(0),
	NONSTOP(1),
	ONE_STOP_OR_FEWER(2),
	TWO_STOPS_OR_FEWER(3);

	public static final Stops DEFAULT = ONE_STOP_OR_FEWER;

	public static Stops defaultIfNull(Stops stops) {
		return stops != null ? stops : DEFAULT;
	}

	private final int option;

	Stops(int option) {
		Assert.isTrue(option > 0, "Option must be greater than 0");
		this.option = option;
	}

	public String getOptionAsString() {
		return String.valueOf(getOption());
	}
}
