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
package com.packt.spring.ai.examples.travel.provider.google.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.VehicleRental;
import com.packt.spring.ai.examples.travel.api.service.TravelService;
import com.packt.spring.ai.examples.travel.provider.google.api.GoogleFlightsApi;
import com.packt.spring.ai.examples.travel.provider.google.api.GoogleHotelsApi;
import com.packt.spring.ai.examples.travel.provider.google.config.SerpApiProperties;
import com.packt.spring.ai.examples.travel.provider.google.model.FlightSearchQuery;
import com.packt.spring.ai.examples.travel.provider.google.model.FlightSearchResults;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import serpapi.SerpApi;

/**
 * {@link TravelService} provider implementation for {@literal Google Flights} using {@literal SerpApi}.
 *
 * @author John Blum
 * @see SerpApi
 * @see GoogleFlightsApi
 * @see GoogleHotelsApi
 * @see SerpApiProperties
 * @see TravelService
 * @see org.springframework.stereotype.Service
 * @see <a href="https://serpapi.com">SerpApi</a>
 * @since 0.1.0
 */
@Service
@Getter(AccessLevel.PROTECTED)
public class GoogleTravelService implements TravelService {

	private final GoogleFlightsApi googleFlights;

	private final GoogleHotelsApi googleHotels;

	private final SerpApi serpApi;

	private final SerpApiProperties serpApiProperties;

	public GoogleTravelService(SerpApiProperties serpApiProperties, GoogleFlightsApi googleFlights, GoogleHotelsApi googleHotels) {
		this.googleFlights = ObjectUtils.requireObject(googleFlights, "Google Flights API is required");
		this.googleHotels = ObjectUtils.requireObject(googleHotels, "Google Hotels API is required");
		this.serpApi = newSerpApi(serpApiProperties);
		this.serpApiProperties = serpApiProperties;
	}

	private SerpApi newSerpApi(SerpApiProperties properties) {

		Assert.notNull(properties, "SerpApiProperties are required");

		Map<String, String> authConfiguration = Map.of(
			"api_key", properties.getApiKey(),
			"engine", properties.getEngine().getGoogle()
		);

		return new SerpApi(authConfiguration);
	}

	@Override
	public List<Flight> searchFlights(FlightSearchRequest request) {

		FlightSearchQuery query = FlightSearchQuery.from(request);
		FlightSearchResults results = getGoogleFlights().search(query);

		return results.toFlights();
	}

	@Override
	public List<HotelBooking> findHotels() {
		return Collections.emptyList();
	}

	@Override
	public List<VehicleRental> rentVehicle() {
		return Collections.emptyList();
	}
}
