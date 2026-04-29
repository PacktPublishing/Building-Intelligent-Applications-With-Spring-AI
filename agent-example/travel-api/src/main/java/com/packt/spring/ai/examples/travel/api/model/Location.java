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

import java.util.Locale;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Constants;

/**
 * Abstract Data Type (ADT) modeling a location as city, area and country, with {@link GpsCoordinates}.
 *
 * @author John Blum
 * @param city {@link String name} of the city, e.g. {@literal Portland, OR}.
 * @param area {@link String name} of the area, such as the state of {@literal California (CA)} in the {@literal USA}
 * or the province of {@literal Ontario} in {@literal Canada}.
 * @param locale {@link Locale} of this location.
 * @param coordinates {@link GpsCoordinates} of this location.
 * @see GpsCoordinates
 * @see Locale
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Location(String city, String area, Locale locale, GpsCoordinates coordinates) {

	public static Location from(GpsCoordinates coordinates) {
		Assert.notNull(coordinates, "GPS coordinates are required");
		return new Location(Constants.UNKNOWN, Constants.UNKNOWN, Locale.getDefault(), coordinates);
	}

	public String getCountry() {
		return locale().getCountry();
	}
}
