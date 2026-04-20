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
package example.service;

import java.util.List;

import example.model.Flight;
import example.model.Hotel;
import example.model.Vehicle;

/**
 * Service interface defining a contract for making travel arrangements.
 *
 * @author John Blum
 * @see Flight
 * @see Hotel
 * @see Vehicle
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface TravelService {

	List<Flight> searchFlights();

	List<Hotel> findHotels();

	List<Vehicle> arrangeForTransportation();

}
