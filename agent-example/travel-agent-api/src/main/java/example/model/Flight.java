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
package example.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a {@literal flight}.
 *
 * @author John Blum
 * @param number {@link String flight number}.
 * @param aircraft make and model of {@link Aircraft} used for the flight.
 * @param airline {@literal carrier} of the flight, such as {@literal American Airlines}.
 * @param departure {@link ZonedDateTime} when the flight leaves.
 * @param arrival {@link ZonedDateTime} when the flight is expected to arrive (land).
 * @param seat {@link Aircraft.Seat} assignment on the flight.
 * @param price {@link BigDecimal Cose} of the flight.
 * @see Aircraft
 * @see Airline
 * @see Location
 * @see ZonedDateTime
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Flight(
	String number,
	Aircraft aircraft,
	Airline airline,
	Location origin,
	Location destination,
	ZonedDateTime departure,
	ZonedDateTime arrival,
	Aircraft.Seat seat,
	BigDecimal price
) {

	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm Z";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public Flight {
		Assert.hasText(number, "Flight number is required");
		Assert.notNull(aircraft, "Aircraft is required");
		Assert.notNull(airline, "Airline is required");
		Assert.notNull(price, "Price is required");
		assertOriginDestination(origin, destination);
		assertDeparture(departure);
		assertArrival(arrival, departure);
	}

	private void assertArrival(ZonedDateTime arrival, ZonedDateTime departure) {

		Assert.notNull(arrival, "Arrival time is required");

		Assert.isTrue(arrival.isAfter(departure), () -> "Arrival [%s] must be after departure [%s]"
			.formatted(arrival.format(DATE_TIME_FORMATTER), departure.format(DATE_TIME_FORMATTER)));
	}

	private void assertDeparture(ZonedDateTime departure) {

		Assert.notNull(departure, "Departure time is required");

		Assert.isTrue(departure.isAfter(ZonedDateTime.now(departure.getZone())),
			() -> "Departure [%s] must be after [%s]".formatted(departure.format(DATE_TIME_FORMATTER),
				ZonedDateTime.now(departure.getZone()).format(DATE_TIME_FORMATTER)));
	}

	private void assertOriginDestination(Location origin, Location destination) {

		Assert.notNull(origin, "Origin is required");
		Assert.notNull(destination, "Destination is required");
		Assert.isFalse(origin.equals(destination), () -> "Origin [%s] cannot be the same as the destination [%s]"
			.formatted(origin, destination));
	}

	public static Flight.Builder builder(String flightNumber) {
		return new Flight.Builder(flightNumber);
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private Aircraft aircraft;

		private Airline airline;

		private BigDecimal price;

		private Aircraft.Seat seat;

		private Location origin;
		private Location destination;

		private final String flightNumber;

		private ZonedDateTime arrival;
		private ZonedDateTime departure;

		protected Builder(String flightNumber) {
			this.flightNumber = StringUtils.requireText(flightNumber, "Flight number is required");
		}

		public Builder arriving(ZonedDateTime arrival) {
			Assert.notNull(arrival, "Arrival time is required");
			this.arrival = arrival;
			return this;
		}

		public Builder departing(ZonedDateTime departure) {
			Assert.notNull(departure, "Departure time is required");
			this.departure = departure;
			return this;
		}

		public Builder flownBy(Airline airline) {
			Assert.notNull(airline, "Airline is required");
			this.airline = airline;
			return this;
		}

		public Builder flying(Aircraft aircraft) {
			this.aircraft = aircraft;
			return this;
		}

		public Builder from(Location origin) {
			Assert.notNull(origin, "Origin is required");
			this.origin = origin;
			return this;
		}

		public Builder price(BigDecimal price) {
			Assert.notNull(price, "Price is required");
			this.price = price;
			return this;
		}

		public Builder sittingIn(Aircraft.Seat seat) {
			this.seat = seat;
			return this;
		}

		public Builder to(Location destination) {
			Assert.notNull(destination, "Destination is required");
			this.destination = destination;
			return this;
		}

		public Flight build() {
			return new Flight(getFlightNumber(), getAircraft(), getAirline(), getOrigin(), getDestination(),
				getDeparture(), getArrival(), getSeat(), getPrice());
		}
	}
}
