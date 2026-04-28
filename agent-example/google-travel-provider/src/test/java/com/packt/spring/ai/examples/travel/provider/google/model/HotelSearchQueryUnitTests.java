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

import com.packt.spring.ai.examples.travel.api.model.Hotel;
import com.packt.spring.ai.examples.travel.api.model.HotelSearchRequest;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link HotelSearchQuery}.
 *
 * @author John Blum
 * @see HotelSearchQuery
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
class HotelSearchQueryUnitTests {

	@Test
	void fromHotelSearchRequest() {

		Hotel hotel = Hotel.from("Marriott");

		HotelSearchRequest request = HotelSearchRequest.stayAt(hotel)
			.checkIn(ZonedDateTime.now().plusDays(2))
			.checkout(ZonedDateTime.now().plusDays(7))
			.pay(BigDecimal.valueOf(200.50d))
			.oneOccupant()
			.build();

		HotelSearchQuery query = HotelSearchQuery.from(request);

		assertThat(query).isNotNull();
		assertThat(query.getHotel()).isEqualTo(hotel.getProviderName());
		assertThat(query.getCheckIn()).isEqualTo(request.getCheckIn());
		assertThat(query.getCheckout()).isEqualTo(request.getCheckout());
		assertThat(query.getOccupants()).isEqualTo(request.getOccupants());
		assertThat(query.getMaxPrice()).isEqualTo(request.getPrice());
	}
}
