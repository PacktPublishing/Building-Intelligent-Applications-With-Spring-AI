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

/**
 * {@link Enum Enumeration} of flight classes, such as {@literal first class}.
 *
 * @author John Blum
 * @see Enum
 * @since 0.1.0
 */
public enum FlightClass {

	BUSINESS,
	FIRST,
	ECONOMY,
	PREMIUM_ECONOMY;

	public static final FlightClass DEFAULT = ECONOMY;

	public static FlightClass defaultIfNull(FlightClass flightClass) {
		return flightClass != null ? flightClass : DEFAULT;
	}
}
