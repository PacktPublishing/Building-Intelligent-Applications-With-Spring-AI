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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.packt.spring.ai.examples.travel.api.model.Airlines;
import com.packt.spring.ai.examples.travel.api.model.Airports;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link FlightSearchQuery}.
 *
 * @author John Blum
 * @see FlightSearchQuery
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
class FlightSearchQueryUnitTests {

	@Test
	void fromFlightSearchRequest() {

		ZonedDateTime departureDateTime = ZonedDateTime.now().plusDays(2);

		FlightSearchRequest request = FlightSearchRequest.builder()
			.departFrom(Airports.PDX)
			.departOn(departureDateTime)
			.arriveAt(Airports.SFO)
			.returnOn(departureDateTime.plusWeeks(1))
			.fly(Airlines.UNITED)
			.inBusinessClass()
			.nonstop()
			.pay(BigDecimal.valueOf(325.50))
			.build();

		FlightSearchQuery query = FlightSearchQuery.from(request);

		assertThat(query).isNotNull();
		assertThat(query.getDeparture()).isEqualTo(Airports.PDX);
		assertThat(query.getOutboundDate()).isEqualTo(departureDateTime);
		assertThat(query.getArrival()).isEqualTo(Airports.SFO);
		assertThat(query.getReturnDate()).isEqualTo(departureDateTime.plusWeeks(1));
		assertThat(query.getAirlines()).containsExactly(Airlines.UNITED);
		assertThat(query.getStops()).isEqualTo(Stops.NONSTOP);
		assertThat(query.getTravelClass()).isEqualTo(TravelClass.BUSINESS);
	}
}
