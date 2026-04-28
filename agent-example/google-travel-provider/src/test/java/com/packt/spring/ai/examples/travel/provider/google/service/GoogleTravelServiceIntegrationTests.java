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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.packt.spring.ai.examples.travel.api.model.Airports;
import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.provider.google.api.GoogleFlightsApi;
import com.packt.spring.ai.examples.travel.provider.google.api.GoogleHotelsApi;
import com.packt.spring.ai.examples.travel.provider.google.config.SerpApiConfiguration;
import com.packt.spring.ai.examples.travel.provider.google.config.SerpApiProperties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests for {@link GoogleTravelService}.
 *
 * @author John Blum
 * @see SpringBootTest
 * @see GoogleTravelService
 * @see org.junit.jupiter.api.Test
 * @since 1.0.0
 */
@SpringBootTest(classes = GoogleTravelServiceIntegrationTests.TestConfiguration.class)
@ActiveProfiles({ "serpapi", "user" })
class GoogleTravelServiceIntegrationTests {

	@Autowired
	private GoogleTravelService travelService;

	@Test
	@EnabledIfSystemProperty(named = "spring-ai-examples-tests", matches = ".*google-travel.*")
	void findsFlights() {

		ZonedDateTime departureDateTime = ZonedDateTime.now().plusMonths(3).plusDays(1);

		FlightSearchRequest request = FlightSearchRequest.builder()
			.departFrom(Airports.PDX)
			.departOn(departureDateTime)
			.arriveAt(Airports.ORD)
			.returnOn(departureDateTime.plusWeeks(1))
			.anyAirline()
			.inBusinessClass()
			.nonstop()
			.anyPrice()
			.build();

		List<Flight> flights = this.travelService.searchFlights(request);

		assertThat(flights).isNotNull();
		assertThat(flights).hasSizeGreaterThan(0);

		flights.forEach(flight -> {
			assertThat(flight.number()).isNotBlank();
			assertThat(flight.airline()).isNotNull();
			assertThat(flight.departure().airport())
				.describedAs("Expected [PDX], but was [%s]", flight.departure().airport().getCode())
				.isEqualTo(Airports.PDX);
			assertThat(flight.departure().dateTime()).isAfter(ZonedDateTime.now().plusMonths(3));
			assertThat(flight.arrival().airport())
				.describedAs("Expected [ORD], but was [%s]", flight.arrival().airport().getCode())
				.isEqualTo(Airports.ORD);
			assertThat(flight.price()).isGreaterThan(BigDecimal.ZERO);
		});
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(SerpApiConfiguration.class)
	static class TestConfiguration {

		@Bean
		GoogleTravelService travelService(SerpApiProperties properties,
				GoogleFlightsApi googleFlights, GoogleHotelsApi googleHotels) {

			return new GoogleTravelService(properties, googleFlights, googleHotels);
		}
	}
}
