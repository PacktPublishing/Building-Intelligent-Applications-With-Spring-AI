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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.packt.spring.ai.examples.travel.api.model.Airline;
import com.packt.spring.ai.examples.travel.api.model.Airport;
import com.packt.spring.ai.examples.travel.api.model.Flight;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.FlightStops;
import com.packt.spring.ai.examples.travel.api.model.Hotel;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.HotelSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.Vehicle;
import com.packt.spring.ai.examples.travel.api.model.VehicleRental;
import com.packt.spring.ai.examples.travel.api.service.TravelService;

import org.cp.elements.lang.ObjectUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Travel Agent implemented using Spring AI.
 *
 * @author John Blum
 * @see Tool
 * @see Service
 * @see Environment
 * @see FlightSearchRequest
 * @see HotelSearchRequest
 * @see TravelService
 * @since 0.1.0
 */
@Service
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class TravelAgent {

	protected static final String MOCK_VEHICLE_RENTAL_PROFILE = "mock-vehicle-rental";

	private final Environment environment;

	private final TravelService travelService;

	public TravelAgent(Environment environment, TravelService travelService) {
		this.environment = ObjectUtils.requireObject(environment, "Environment is required");
		this.travelService = ObjectUtils.requireObject(travelService, "TravelService is required");
	}

	@SuppressWarnings("all")
	@Tool(name = "flight-search", description = "Search for flights")
	public List<Flight> searchFlights(
		@ToolParam(description = "Departure airport") String departureAirport,
		@ToolParam(description = "Departure date/time in format yyyy-MM-dd, ignore time, for example 2026-05-21") LocalDate departureDate,
		@ToolParam(description = "Arrival airport") String arrivalAirport,
		@ToolParam(description = "Return date/time in format yyyy-MM-dd, ignore time, for example 2026-06-02") LocalDate returnDate,
		@ToolParam(description = "Name of airline") String airlineName,
		@ToolParam(description = "Number of stops; if nonstop or no stops, then 0") int stops
	) {

		Airport destinationAirport = Airport.from(arrivalAirport);

		FlightSearchRequest request = FlightSearchRequest.builder()
			.departFrom(Airport.from(departureAirport))
			.departOn(departureDate.atTime(LocalTime.MIN).atZone(inLocalZone()))
			.arriveAt(destinationAirport)
			.returnOn(returnDate.atTime(LocalTime.MAX).atZone(inReturnZone(destinationAirport)))
			.fly(Airline.from(airlineName))
			.inPremiumEconomy()
			.stops(resolveStops(stops))
			.anyPrice()
			.build();

		List<Flight> flights = getTravelService().searchFlights(request);

		return flights;
	}

	private FlightStops resolveStops(int stops) {

		return switch (stops) {
			case 0 -> FlightStops.NONSTOP;
			case 1 -> FlightStops.ONE_STOP;
			default -> FlightStops.ANY;
		};
	}

	@SuppressWarnings("all")
	@Tool(name = "hotel-finder", description = "Find a hotel")
	public List<HotelBooking> findHotels(
		@ToolParam(description = "Name of the hotel to search") String hotelName,
		@ToolParam(description = "Check-in date") LocalDate checkIn,
		@ToolParam(description = "Checkout date") LocalDate checkout,
		@ToolParam(description = "Number of occupants") Integer occupants
	) {

		HotelSearchRequest request = HotelSearchRequest.stayAt(Hotel.from(hotelName))
			.checkIn(checkIn.atStartOfDay(inLocalZone()))
			.checkout(checkout.atTime(LocalTime.MAX).atZone(inLocalZone()))
			.payAny()
			.withOccupants(occupants)
			.build();

		List<HotelBooking> hotelBookings = getTravelService().findHotels(request);

		return hotelBookings;
	}

	@Tool(name = "rent-vehicle", description = "Rent a vehicle, such as a car")
	public List<VehicleRental> rentVehicle() {

		if (isMockVehicleRentalProfileEnabled()) {
			return List.of(mockVehicleRental());
		}

		throw new UnsupportedOperationException("Vehicle rental is not implemented");
	}

	private ZoneId inLocalZone() {
		return ZoneId.systemDefault();
	}

	// TODO: Return ZoneId using Airport code of destination (arrival airport)
	private ZoneId inReturnZone(Airport airport) {
		return inLocalZone();
	}

	private boolean isMockVehicleRentalProfileEnabled() {
		return getEnvironment().matchesProfiles(MOCK_VEHICLE_RENTAL_PROFILE);
	}

	private VehicleRental mockVehicleRental() {

		ZonedDateTime now = ZonedDateTime.now();

		return VehicleRental.builder(new Vehicle(Year.of(2021), "Audi", "R8", 5_000, Vehicle.Type.COUPE))
			.pickingUp(now.plusWeeks(1))
			.droppingOff(now.plusWeeks(1).plusDays(5))
			.price(BigDecimal.valueOf(2_000))
			.build();
	}
}
