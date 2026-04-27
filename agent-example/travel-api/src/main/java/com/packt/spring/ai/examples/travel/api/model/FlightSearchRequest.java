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
package com.packt.spring.ai.examples.travel.api.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.util.CollectionUtils;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) modeling a request to search for flights.
 *
 * @author John Blum
 * @see Airport
 * @see FlightType
 * @see ZonedDateTime
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class FlightSearchRequest {

	protected static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm z";

	protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	private static String format(ZonedDateTime dateTime) {
		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private final Arrival arrival;

	private BigDecimal price;

	private final Departure departure;

	private FlightClass flightClass;

	private FlightType flightType;

	private List<Airline> airlines;

	private ZonedDateTime returnDateTime;

	protected FlightSearchRequest(Departure departure, Arrival arrival) {
		this.departure = ObjectUtils.requireObject(departure, "Departure is required");
		this.arrival = ObjectUtils.requireObject(arrival, "Arrival is required");
		arrival.assertArrival(departure);
	}

	public Airport getArrivalAirport() {
		return getArrival().airport();
	}

	public Airport getDepartureAirport() {
		return getDeparture().airport();
	}

	public ZonedDateTime getDepartureDateTime() {
		return getDeparture().dateTime();
	}

	public FlightType getFlightType() {

		FlightType configuredFlightType = this.flightType;

		return configuredFlightType != null ? configuredFlightType
			: hasReturnDate() ? FlightType.ROUND_TRIP
			: FlightType.ONE_WAY;
	}

	public boolean hasReturnDate() {
		return Objects.nonNull(getReturnDateTime());
	}

	public FlightSearchRequest flying(Airlines... airlines) {
		return flying(List.of(airlines));
	}

	public FlightSearchRequest flying(List<Airline> airlines) {
		this.airlines = CollectionUtils.nullSafeList(airlines);
		return this;
	}

	public FlightSearchRequest flying(@Nullable FlightClass flightClass) {
		this.flightClass = FlightClass.defaultIfNull(flightClass);
		return this;
	}

	public FlightSearchRequest pay(@Nullable BigDecimal price) {
		this.price = price;
		return this;
	}

	public FlightSearchRequest returnOn(@Nullable ZonedDateTime dateTime) {
		this.returnDateTime = dateTime;
		return this;
	}

	public FlightSearchRequest withFlightType(FlightType flightType) {
		this.flightType = FlightType.defaultIfNull(flightType);
		return this;
	}

	protected record Arrival(Airport airport, @Nullable ZonedDateTime dateTime) {

		protected Arrival {
			Assert.notNull(airport, "Arrival airport is required");
		}

		protected static Arrival.Builder arrivingAt(Airport airport) {
			return new Arrival.Builder(airport);
		}

		protected Arrival assertArrival(Departure departure) {

			Assert.notNull(departure, "Departure is required");

			Assert.isFalse(airport().equals(departure.airport()),
				() -> "Arrival airport [%s] cannot be the same as Departure airport [%s]"
					.formatted(airport(), departure.airport()));

			ZonedDateTime arrivalDateTime = dateTime();

			if (arrivalDateTime != null) {
				Assert.isTrue(arrivalDateTime.isAfter(departure.dateTime()),
					() -> "Arrival date/time [%s] cannot be before [%s]"
						.formatted(format(dateTime()), format(departure.dateTime())));
			}

			return this;
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		protected static class Builder {

			private final Airport airport;

			protected Arrival on(ZonedDateTime dateTime) {
				return new Arrival(this.airport, dateTime);
			}

			protected Arrival build() {
				return new Arrival(this.airport, null);
			}
		}
	}

	protected record Departure(Airport airport, ZonedDateTime dateTime) {

		protected Departure {
			Assert.notNull(airport, "Departure airport is required");
			Assert.notNull(dateTime, "Departure date/time is required");
			Assert.isTrue(dateTime.isAfter(ZonedDateTime.now()), () -> "Departure date/time [%s] must be after [%s]"
				.formatted(format(dateTime), format(ZonedDateTime.now())));
		}

		protected static Departure.Builder departingFrom(Airport airport) {
			return new Departure.Builder(airport);
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		protected static class Builder {

			private final Airport airport;

			protected Departure on(ZonedDateTime dateTime) {
				return new Departure(this.airport, dateTime);
			}
		}
	}

	public interface DepartureBuilder {
		DepartureDateTimeBuilder departingFrom(Airport airport);
	}

	public interface DepartureDateTimeBuilder {
		ArrivalBuilder departingOn(ZonedDateTime dateTime);
	}

	public interface ArrivalBuilder {
		ReturnDateTimeBuilder arrivingAt(Airport airport);
	}

	public interface ReturnDateTimeBuilder {

		AirlineBuilder returningOn(ZonedDateTime dateTime);

		default AirlineBuilder noReturn() {
			return returningOn(null);
		}
	}

	public interface AirlineBuilder {

		FlightClassBuilder flying(Airline... airlines);

		default FlightClassBuilder anyAirline() {
			return flying();
		}
	}

	public interface FlightClassBuilder {

		PriceBuilder in(FlightClass flightClass);

		default PriceBuilder inBusinessClass() {
			return in(FlightClass.BUSINESS);
		}

		default PriceBuilder inFirstClass() {
			return in(FlightClass.FIRST);
		}

		default PriceBuilder inEconomy() {
			return in(FlightClass.ECONOMY);
		}

		default PriceBuilder inPremiumEconomy() {
			return in(FlightClass.PREMIUM_ECONOMY);
		}
	}

	public interface PriceBuilder {
		InFlightBuilder pay(BigDecimal price);
	}

	public interface InFlightBuilder {
		FlightSearchRequest build();
	}

	@Getter
	protected static class Builder implements DepartureBuilder, DepartureDateTimeBuilder, ArrivalBuilder,
			ReturnDateTimeBuilder, AirlineBuilder, FlightClassBuilder, PriceBuilder, InFlightBuilder {

		private Airport arrivalAirport;
		private Airport departureAirport;

		private BigDecimal price;

		private FlightClass flightClass;

		private List<Airline> airlines;

		private ZonedDateTime departingDateTime;
		private ZonedDateTime returningDateTime;

		@Override
		public ReturnDateTimeBuilder arrivingAt(Airport airport) {
			this.arrivalAirport = ObjectUtils.requireObject(airport, "Arrival airport is required");
			return this;
		}

		@Override
		public DepartureDateTimeBuilder departingFrom(Airport airport) {
			this.departureAirport = ObjectUtils.requireObject(airport, "Departure airport is required");
			return this;
		}

		@Override
		public ArrivalBuilder departingOn(ZonedDateTime dateTime) {
			return this;
		}

		@Override
		public FlightClassBuilder flying(Airline... airlines) {
			this.airlines = List.of(airlines);
			return this;
		}

		@Override
		public PriceBuilder in(FlightClass flightClass) {
			this.flightClass = flightClass;
			return this;
		}

		@Override
		public InFlightBuilder pay(@Nullable BigDecimal price) {
			this.price = price;
			return this;
		}

		@Override
		public AirlineBuilder returningOn(@Nullable ZonedDateTime dateTime) {
			this.returningDateTime = dateTime;
			return this;
		}

		@Override
		public FlightSearchRequest build() {

			Departure departure = Departure.departingFrom(getDepartureAirport()).on(getDepartingDateTime());
			Arrival arrival = Arrival.arrivingAt(getArrivalAirport()).build();

			return new FlightSearchRequest(departure, arrival)
				.returnOn(getReturningDateTime())
				.flying(getAirlines())
				.flying(getFlightClass())
				.pay(getPrice());
		}
	}
}
