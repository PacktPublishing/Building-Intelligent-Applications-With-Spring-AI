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
package com.packt.spring.ai.examples.travel.api.service;

import java.time.ZonedDateTime;
import java.util.List;

import com.packt.spring.ai.examples.travel.api.model.Airport;
import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.Hotel;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.HotelSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.VehicleRental;
import com.packt.spring.ai.examples.travel.api.model.VehicleSearchRequest;

/**
 * Service interface defining a contract for making travel arrangements.
 *
 * @author John Blum
 * @see Flight
 * @see HotelBooking
 * @see VehicleRental
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface TravelService {

	/**
	 * Searches for available {@link Flight Flights} departing from {@link Airport} at a specified {@link ZonedDateTime}
	 * and arriving at a designated {@link Airport}.
	 *
	 * @param request {@link FlightSearchRequest} defining the search criteria used to match {@link Flight Flights}.
	 * @return a {@link List} of all matching {@link Flight Flights}.
	 * @see FlightSearchRequest
	 * @see Flight
	 * @see List
	 */
	List<Flight> searchFlights(FlightSearchRequest request);

	/**
	 * Find {@link Hotel Hotels} with vacancy matching the given {@link HotelSearchRequest}.
	 *
	 * @param request {@link HotelSearchRequest} defining search criteria used to match available {@link Hotel Hotels}.
	 * @return a {@link List} of {@link HotelBooking HotelBookings} available.
	 * @see HotelSearchRequest
	 * @see HotelBooking
	 * @see List
	 */
	List<HotelBooking> findHotels(HotelSearchRequest request);

	/**
	 * Search for available {@link VehicleRental VehicleRentals} matching the given {@link VehicleSearchRequest}.
	 *
	 * @param request {@link VehicleSearchRequest} defining search criteria used to match a {@link VehicleRental}.
	 * @return a {@link List} of matching {@link VehicleRental VehicleRentals}.
	 * @throws UnsupportedOperationException by default.
	 * @see VehicleSearchRequest
	 * @see VehicleRental
	 * @see List
	 */
	default List<VehicleRental> rentVehicle(VehicleSearchRequest request) {
		throw new UnsupportedOperationException("Vehicle rentals not supported");
	}
}
