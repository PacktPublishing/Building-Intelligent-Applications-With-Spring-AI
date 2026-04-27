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

import java.util.List;

import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.VehicleRental;

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

	List<Flight> searchFlights(FlightSearchRequest searchRequest);

	List<HotelBooking> findHotels();

	default List<VehicleRental> rentVehicle() {
		throw new UnsupportedOperationException("Vehicle rental is not supported");
	}
}
