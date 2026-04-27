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
package com.packt.spring.ai.examples.travel.agent;

import java.util.Collections;
import java.util.List;

import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.VehicleRental;

import org.springframework.ai.tool.annotation.Tool;

/**
 * Travel Agent implemented using Spring AI.
 *
 * @author John Blum
 * @see org.springframework.ai.tool.annotation.Tool
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class TravelAgent {

	@Tool(description = "Search for flights")
	public List<Flight> searchFlights() {
		return Collections.emptyList();
	}

	@Tool(description = "Find a hotel")
	public List<HotelBooking> findHotels() {
		return Collections.emptyList();
	}

	@Tool(description = "Rent a vehicle, such as a car")
	public List<VehicleRental> rentVehicle() {
		return Collections.emptyList();
	}
}
