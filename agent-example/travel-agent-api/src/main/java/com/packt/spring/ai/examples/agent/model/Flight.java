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
package com.packt.spring.ai.examples.agent.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a {@literal flight}.
 *
 * @author John Blum
 * @param number {@link String flight number}.
 * @param departure {@link Departure} {@link ZonedDateTime when} the flight leaves from the {@link Location origin}.
 * @param arrival {@link ZonedDateTime} {@link ZonedDateTime when} the flight arrives at the {@link Location destination}.
 * @param aircraft make and model of {@link Aircraft} used for the flight.
 * @param seat {@link Aircraft.Seat} assignment on the flight.
 * @param airline {@literal carrier} of the flight, such as {@literal American Airlines}.
 * @param price {@link BigDecimal Cose} of the flight.
 * @see Aircraft
 * @see Airline
 * @see Arrival
 * @see Departure
 * @see Location
 * @see BigDecimal
 * @see ZonedDateTime
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Flight(
	String number,
	Departure departure,
	Arrival arrival,
	Aircraft aircraft,
	Aircraft.Seat seat,
	Airline airline,
	BigDecimal price
) {

	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm Z";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public static Flight.Builder builder(String flightNumber) {
		return new Flight.Builder(flightNumber);
	}

	public Flight {
		Assert.hasText(number, "Flight number is required");
		Assert.notNull(aircraft, "Aircraft is required");
		Assert.notNull(airline, "Airline is required");
		Assert.notNull(price, "Price is required");
		assertDeparture(departure);
		assertArrival(arrival, departure);
	}

	private void assertArrival(Arrival arrival, Departure departure) {

		Assert.notNull(arrival, "Arrival is required");

		ZonedDateTime arrivalDateTime = arrival.dateTime();
		ZonedDateTime departureDateTime = departure.dateTime();

		Assert.isTrue(arrivalDateTime.isAfter(departureDateTime), () -> "Arrival [%s] must be after departure [%s]"
			.formatted(arrivalDateTime.format(DATE_TIME_FORMATTER), departureDateTime.format(DATE_TIME_FORMATTER)));

		Assert.isFalse(arrival.arrivingAt().equals(departure.origin()),
			() -> "Destination [%s] must be different than the origin [%s]"
				.formatted(arrival().arrivingAt(), departure.origin()));
	}

	private void assertDeparture(Departure departure) {

		Assert.notNull(departure, "Departure is required");

		ZonedDateTime departureDateTime = departure.dateTime();
		ZonedDateTime now = ZonedDateTime.now(departureDateTime.getZone());

		Assert.isTrue(departureDateTime.isAfter(now), () -> "Departure [%s] must be after [%s]"
				.formatted(departureDateTime.format(DATE_TIME_FORMATTER), now.format(DATE_TIME_FORMATTER)));
	}

	public <T> T reserve(Function<Flight, T> callback) {
		return callback.apply(this);
	}

	public record Arrival(Airport arrivingAt, ZonedDateTime dateTime) {

		public static Arrival.Builder arrivingAt(Airport destination) {
			return new Arrival.Builder(destination);
		}

		public static class Builder {

			private final Airport destination;

			protected Builder(Airport destination) {
				this.destination = ObjectUtils.requireObject(destination, "Destination is required");
			}

			public Arrival on(ZonedDateTime dateTime) {
				Assert.notNull(dateTime, "Arrival date/time is required");
				return new Arrival(this.destination, dateTime);
			}
		}

		@Override
		@SuppressWarnings("all")
		public String toString() {
			return "Arriving at [%s] on [%s]"
				.formatted(arrivingAt(), dateTime().format(DATE_TIME_FORMATTER));
		}
	}

	public record Departure(Airport origin, ZonedDateTime dateTime) {

		public static Departure.Builder departingFrom(Airport origin) {
			return new Departure.Builder(origin);
		}

		public static class Builder {

			private final Airport origin;

			protected Builder(Airport origin) {
				this.origin = ObjectUtils.requireObject(origin, "Origin is required");
			}

			public Departure on(ZonedDateTime dateTime) {
				Assert.notNull(dateTime, "Departure date/time is required");
				return new Departure(this.origin, dateTime);
			}
		}

		@Override
		@SuppressWarnings("all")
		public String toString() {
			return "Departing from [%s] at [%s]"
				.formatted(origin(), dateTime().format(DATE_TIME_FORMATTER));
		}
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private Aircraft aircraft;

		private Airline airline;

		private BigDecimal price;

		private Aircraft.Seat seat;

		private Airport fromOrigin;
		private Airport toDestination;

		private final String flightNumber;

		private ZonedDateTime arrivalDateTime;
		private ZonedDateTime departureDateTime;

		protected Builder(String flightNumber) {
			this.flightNumber = StringUtils.requireText(flightNumber, "Flight number is required");
		}

		public Builder arrivingAt(ZonedDateTime arrival) {
			Assert.notNull(arrival, "Arrival time is required");
			this.arrivalDateTime = arrival;
			return this;
		}

		public Builder departingAt(ZonedDateTime departure) {
			Assert.notNull(departure, "Departure time is required");
			this.departureDateTime = departure;
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

		public Builder from(Airport origin) {
			Assert.notNull(origin, "Origin is required");
			this.fromOrigin = origin;
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

		public Builder to(Airport destination) {
			Assert.notNull(destination, "Destination is required");
			this.toDestination = destination;
			return this;
		}

		public Flight build() {

			Departure departure = Departure.departingFrom(getFromOrigin()).on(getDepartureDateTime());
			Arrival arrival = Arrival.arrivingAt(getToDestination()).on(getArrivalDateTime());

			return new Flight(getFlightNumber(), departure, arrival, getAircraft(), getSeat(), getAirline(), getPrice());
		}
	}
}
