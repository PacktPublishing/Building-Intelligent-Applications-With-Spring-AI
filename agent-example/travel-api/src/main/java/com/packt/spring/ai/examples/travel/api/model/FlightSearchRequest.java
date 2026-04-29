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

	public static FlightSearchRequest.DepartureBuilder builder() {
		return new FlightSearchRequest.Builder();
	}

	private static String format(ZonedDateTime dateTime) {
		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private final Arrival arrival;

	private BigDecimal price;

	private final Departure departure;

	private FlightClass flightClass;

	private FlightStops flightStops;

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
			: isReturnFlight() ? FlightType.ROUND_TRIP
			: FlightType.ONE_WAY;
	}

	public boolean isReturnFlight() {
		return Objects.nonNull(getReturnDateTime());
	}

	public FlightSearchRequest fly(Airlines... airlines) {
		return fly(List.of(airlines));
	}

	public FlightSearchRequest fly(List<Airline> airlines) {
		this.airlines = CollectionUtils.nullSafeList(airlines);
		return this;
	}

	public FlightSearchRequest fly(@Nullable FlightClass flightClass) {
		this.flightClass = FlightClass.defaultIfNull(flightClass);
		return this;
	}

	public FlightSearchRequest fly(@Nullable FlightStops flightStops) {
		this.flightStops = FlightStops.defaultIfNull(flightStops);
		return this;
	}

	public FlightSearchRequest fly(@Nullable FlightType flightType) {
		this.flightType = FlightType.defaultIfNull(flightType);
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

	protected record Arrival(Airport airport, @Nullable ZonedDateTime dateTime) {

		protected Arrival {
			Assert.notNull(airport, "Arrival airport is required");
		}

		protected Arrival assertArrival(Departure departure) {

			Assert.notNull(departure, "Departure is required");

			Assert.isFalse(airport().equals(departure.airport()),
				() -> "Arrival airport [%s] cannot be the same as Departure airport [%s]"
					.formatted(airport(), departure.airport()));

			ZonedDateTime arrivalDateTime = dateTime();

			if (arrivalDateTime != null) {
				Assert.isTrue(arrivalDateTime.isAfter(departure.dateTime()),
					() -> "Arrival date/time [%s] must be after Departure date/time [%s]"
						.formatted(format(dateTime()), format(departure.dateTime())));
			}

			return this;
		}

		protected static Arrival.Builder arriveAt(Airport airport) {
			return new Arrival.Builder(airport);
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		protected static class Builder {

			private final Airport airport;

			protected Arrival on(@Nullable ZonedDateTime dateTime) {
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

		protected static Departure.Builder departFrom(Airport airport) {
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
		DepartureDateTimeBuilder departFrom(Airport airport);
	}

	public interface DepartureDateTimeBuilder {
		ArrivalBuilder departOn(ZonedDateTime dateTime);
	}

	public interface ArrivalBuilder {
		ReturnDateTimeBuilder arriveAt(Airport airport);
	}

	public interface ReturnDateTimeBuilder {

		AirlineBuilder returnOn(ZonedDateTime dateTime);

		default AirlineBuilder noReturn() {
			return returnOn(null);
		}
	}

	public interface AirlineBuilder {

		FlightClassBuilder fly(Airline... airlines);

		default FlightClassBuilder anyAirline() {
			return fly();
		}
	}

	public interface FlightClassBuilder {

		LayoverBuilder in(FlightClass flightClass);

		default LayoverBuilder inBusinessClass() {
			return in(FlightClass.BUSINESS);
		}

		default LayoverBuilder inFirstClass() {
			return in(FlightClass.FIRST);
		}

		default LayoverBuilder inEconomy() {
			return in(FlightClass.ECONOMY);
		}

		default LayoverBuilder inPremiumEconomy() {
			return in(FlightClass.PREMIUM_ECONOMY);
		}
	}

	public interface LayoverBuilder {

		PriceBuilder stops(FlightStops flightStops);

		default PriceBuilder nonstop() {
			return stops(FlightStops.NONSTOP);
		}

		default PriceBuilder oneStop() {
			return stops(FlightStops.ONE_STOP);
		}

		default PriceBuilder unlimitedStops() {
			return stops(FlightStops.ANY);
		}
	}

	public interface PriceBuilder {

		RequestBuilder pay(BigDecimal price);

		default RequestBuilder anyPrice() {
			return pay(BigDecimal.valueOf(Double.MAX_VALUE));
		}
	}

	public interface RequestBuilder {
		FlightSearchRequest build();
	}

	@Getter
	protected static class Builder implements DepartureBuilder, DepartureDateTimeBuilder, ArrivalBuilder,
			ReturnDateTimeBuilder, AirlineBuilder, FlightClassBuilder, LayoverBuilder, PriceBuilder, RequestBuilder {

		private Airport arrivalAirport;
		private Airport departureAirport;

		private BigDecimal price;

		private FlightClass flightClass;

		private FlightStops flightStops;

		private List<Airline> airlines;

		private ZonedDateTime departureDateTime;
		private ZonedDateTime returnDateTime;

		@Override
		public ReturnDateTimeBuilder arriveAt(Airport airport) {
			this.arrivalAirport = ObjectUtils.requireObject(airport, "Arrival airport is required");
			return this;
		}

		@Override
		public DepartureDateTimeBuilder departFrom(Airport airport) {
			this.departureAirport = ObjectUtils.requireObject(airport, "Departure airport is required");
			return this;
		}

		@Override
		public ArrivalBuilder departOn(ZonedDateTime dateTime) {
			this.departureDateTime = dateTime;
			return this;
		}

		@Override
		public FlightClassBuilder fly(Airline... airlines) {
			this.airlines = List.of(airlines);
			return this;
		}

		@Override
		public LayoverBuilder in(FlightClass flightClass) {
			this.flightClass = flightClass;
			return this;
		}

		@Override
		public RequestBuilder pay(@Nullable BigDecimal price) {
			this.price = price;
			return this;
		}

		@Override
		public AirlineBuilder returnOn(@Nullable ZonedDateTime dateTime) {
			this.returnDateTime = dateTime;
			return this;
		}

		@Override
		public PriceBuilder stops(FlightStops flightStops) {
			this.flightStops = flightStops;
			return this;
		}

		@Override
		public FlightSearchRequest build() {

			Departure departure = Departure.departFrom(getDepartureAirport()).on(getDepartureDateTime());
			Arrival arrival = Arrival.arriveAt(getArrivalAirport()).build();

			return new FlightSearchRequest(departure, arrival)
				.returnOn(getReturnDateTime())
				.fly(getAirlines())
				.fly(getFlightClass())
				.fly(getFlightStops())
				.pay(getPrice());
		}
	}
}
