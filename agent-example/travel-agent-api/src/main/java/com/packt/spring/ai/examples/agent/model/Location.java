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
package com.packt.spring.ai.examples.agent.model;

import java.util.Locale;

/**
 * Abstract Data Type (ADT) and Java record modeling a location as city and country.
 *
 * @author John Blum
 * @param cityName {@link String name} of the city, e.g. {@literal Portland, OR}.
 * @param locale {@link Locale} of this location.
 * @see GeographicCoordinates
 * @see Locale
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Location(String cityName, Locale locale, GeographicCoordinates coordinates) {

	public String getCountry() {
		return locale().getCountry();
	}
}
