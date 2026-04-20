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
package com.packt.spring.ai.examples.agent.service;

import java.util.List;

import com.packt.spring.ai.examples.agent.model.FlightReservation;
import com.packt.spring.ai.examples.agent.model.Hotel;
import com.packt.spring.ai.examples.agent.model.Vehicle;

/**
 * Service interface defining a contract for making travel arrangements.
 *
 * @author John Blum
 * @see FlightReservation
 * @see Hotel
 * @see Vehicle
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface TravelService {

	List<FlightReservation> searchFlights();

	List<Hotel> findHotels();

	List<Vehicle> arrangeForTransportation();

}
