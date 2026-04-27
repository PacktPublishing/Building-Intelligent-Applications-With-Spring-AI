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

import org.cp.elements.lang.StringUtils;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of {@link Airline Airlines}.
 * <p>
 * This enumeration is not complete.
 *
 * @author John Blum
 * @see Airline
 * @see Enum
 * @see <a href="https://en.wikipedia.org/wiki/List_of_airline_codes">List of airline codes</a>
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public enum Airlines implements Airline {

	AIR_CANADA("Air Canada", "AC"),
	AIR_FRANCE("Air France", "AF"),
	AIR_NEW_ZEALAND("Air New Zealand", "NZ"),
	ALASKAN("Alaskan Airlines", "AS"),
	AMERICAN("American Airlines", "AA"),
	AMERICAN_EAGLE("American Eagle Airlines", "MQ"),
	BRITISH_AIRWAYS("British Airways", "BA"),
	DELTA("Delta Airlines", "DL"),
	EMIRATES("Emirates", "EK"),
	FRONTIER("Frontier Airlines", "F9"),
	HAWAIIAN("Hawaiian Airlines", "HA"),
	JAPAN("Japan Airlines", "JL"),
	JET_BLUE("Jet Blue Airlines", "B6"),
	LUFTHANSA("Lufthansa", "LH"),
	NORWEGIAN("Norwegian", "DI"),
	SOUTHWEST("Southwest Airlines", "WN"),
	UNITED("United Airlines", "UA");

	private final String name;
	private final String carrierCode;

	Airlines(String name, String carrierCode) {
		this.name = StringUtils.requireText(name, "Name of airlines is required");
		this.carrierCode = StringUtils.requireText(carrierCode, "Carrier code of airline is required");
	}

	@Override
	public String getCarrierCode() {
		return this.carrierCode;
	}

	@Override
	public String getShortName() {
		String enumName = this.name();
		return StringUtils.capitalize(enumName).replaceAll("_", " ");
	}
}
