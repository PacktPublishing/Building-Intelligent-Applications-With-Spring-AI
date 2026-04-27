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
package com.packt.spring.ai.examples.travel.provider.google.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

import com.packt.spring.ai.examples.travel.api.model.Airport;
import com.packt.spring.ai.examples.travel.api.model.FlightSearchRequest;
import com.packt.spring.ai.examples.travel.api.model.FlightType;
import com.packt.spring.ai.examples.travel.provider.google.config.SerpApiProperties;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a search query for {@literal Google Flights} using the {@literal SerpApi}.
 *
 * @author John Blum
 * @see FlightSearchRequest
 * @see SerpApiProperties
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class FlightSearchQuery {

	protected static final String DATE_PATTERN = "yyyy-MM-dd";
	protected static final String DATE_TIME_PATTERN = DATE_PATTERN.concat(" HH:mm z");

	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
	protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public static FlightSearchQuery.Builder builder() {
		return new FlightSearchQuery.Builder();
	}

	public static FlightSearchQuery from(FlightSearchRequest searchRequest) {

		Assert.notNull(searchRequest, "FlightSearchRequest is required");

		return builder()
			.departingFrom(searchRequest.getDepartureAirport())
			.departingOn(searchRequest.getDepartureDateTime())
			.arrivingAt(searchRequest.getArrivalAirport())
			.returningOn(searchRequest.getReturnDateTime())
			.sittingIn(resolveTravelClass(searchRequest))
			.build();
	}

	private static String formatDate(ZonedDateTime dateTime) {
		return dateTime.format(DATE_FORMATTER);
	}

	private static String formatDateTime(ZonedDateTime dateTime) {
		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private static TravelClass resolveTravelClass(FlightSearchRequest searchRequest) {
		return switch(searchRequest.getFlightClass()) {
			case BUSINESS -> TravelClass.BUSINESS;
			case FIRST -> TravelClass.FIRST;
			case ECONOMY -> TravelClass.ECONOMY;
			case PREMIUM_ECONOMY -> TravelClass.PREMIUM_ECONOMY;
		};
	}

	private final Airport departure;
	private final Airport arrival;

	private TravelClass travelClass;

	private final ZonedDateTime outboundDate;
	private final ZonedDateTime returnDate;

	protected FlightSearchQuery(
		Airport departure,
		ZonedDateTime outboundDate,
		Airport arrival,
		ZonedDateTime returnDate
	) {
		this.departure = ObjectUtils.requireObject(departure, "Departure Airport is required");
		this.arrival = assertArrival(arrival, departure);
		this.outboundDate = assertOutboundDate(outboundDate);
		this.returnDate = assertReturnDate(returnDate, outboundDate);
	}

	private Airport assertArrival(Airport arrival, Airport departure) {
		Assert.isFalse(arrival.equals(departure), "Arrival airport [%s] cannot be the same as Departure airport [%s]",
			arrival, departure);
		return arrival;
	}

	private ZonedDateTime assertOutboundDate(ZonedDateTime outboundDate) {
		ZonedDateTime now = ZonedDateTime.now();
		Assert.notNull(outboundDate, "Outbound date/time is required");
		Assert.isTrue(outboundDate.isAfter(now), () -> "Outbound date [%s] must be after [%s]"
			.formatted(formatDateTime(outboundDate), formatDateTime(now)));
		return outboundDate;
	}

	private ZonedDateTime assertReturnDate(ZonedDateTime returnDate, ZonedDateTime outboundDate) {
		Assert.notNull(returnDate, "Return date/time is required");
		Assert.isTrue(returnDate.isAfter(outboundDate), () -> "Return date [%s] must be after Outbound date [%s]"
			.formatted(formatDateTime(returnDate), formatDateTime(outboundDate)));
		return returnDate;
	}

	public FlightSearchQuery sittingIn(TravelClass travelClass) {
		this.travelClass = travelClass;
		return this;
	}

	public interface DepartureBuilder {
		DepartureTimeBuilder departingFrom(Airport airport);
	}

	public interface DepartureTimeBuilder {
		ArrivalBuilder departingOn(ZonedDateTime dateTime);
	}

	public interface ArrivalBuilder {
		ReturnTimeBuilder arrivingAt(Airport airport);
	}

	public interface ReturnTimeBuilder {
		InFlightBuilder returningOn(ZonedDateTime dateTime);
	}

	public interface InFlightBuilder {
		InFlightBuilder sittingIn(TravelClass travelClass);
		FlightSearchQuery build();
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder
			implements DepartureBuilder, DepartureTimeBuilder, ArrivalBuilder, ReturnTimeBuilder, InFlightBuilder {

		private Airport departure;
		private Airport arrival;

		private TravelClass travelClass;

		private ZonedDateTime departureDateTime;
		private ZonedDateTime returnDateTime;

		@Override
		public ReturnTimeBuilder arrivingAt(Airport airport) {
			this.arrival = ObjectUtils.requireObject(airport, "Arrival airport is required");
			return this;
		}

		@Override
		public ArrivalBuilder departingOn(ZonedDateTime dateTime) {
			this.departureDateTime = ObjectUtils.requireObject(dateTime, "Departure date/time is required");
			return this;
		}

		@Override
		public DepartureTimeBuilder departingFrom(Airport airport) {
			this.departure = ObjectUtils.requireObject(airport, "Departure airport is required");
			return this;
		}

		@Override
		public InFlightBuilder returningOn(ZonedDateTime dateTime) {
			this.returnDateTime = ObjectUtils.requireObject(dateTime, "Return date/time is required");
			return this;
		}

		@Override
		public InFlightBuilder sittingIn(TravelClass travelClass) {
			this.travelClass = travelClass;
			return this;
		}

		@Override
		public FlightSearchQuery build() {
			return new FlightSearchQuery(getDeparture(), getDepartureDateTime(), getArrival(), getReturnDateTime());
		}
	}

	@Getter(AccessLevel.PROTECTED)
	public static class FlightSearchRequestArgumentResolver implements HttpServiceArgumentResolver {

		private final SerpApiProperties properties;

		public FlightSearchRequestArgumentResolver(SerpApiProperties properties) {
			this.properties = ObjectUtils.requireObject(properties, "SerpApiProperties are required");
		}

		@Override
		@SuppressWarnings("all")
		public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {

			if (parameter.getParameterType().equals(FlightSearchQuery.class)) {
				FlightSearchQuery request = (FlightSearchQuery) argument;
				requestValues.addRequestParameter("api_key", getProperties().getApiKey());
				requestValues.addRequestParameter("engine", getProperties().getEngine().getFlights());
				requestValues.addRequestParameter("output", getProperties().getOutput());
				requestValues.addRequestParameter("arrival_id", request.getArrival().getCode());
				requestValues.addRequestParameter("currentcy", resolveCurrency());
				requestValues.addRequestParameter("departure_id", request.getDeparture().getCode());
				requestValues.addRequestParameter("outbound_date", formatDate(request.getOutboundDate()));
				requestValues.addRequestParameter("return_date", formatDate(request.getReturnDate()));
				requestValues.addRequestParameter("sort_by", SortBy.PRICE.getOptionAsString());
				requestValues.addRequestParameter("stops", Stops.ONE_STOP_OR_FEWER.getOptionAsString());
				requestValues.addRequestParameter("travel_class", resolveTravelClass(request.getTravelClass()));
				requestValues.addRequestParameter("type", resolveFlightType(FlightType.ROUND_TRIP));
				return true;
			}

			return false;
		}

		private String resolveCurrency() {
			return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
		}

		private String resolveFlightType(FlightType flightType) {
			return switch (FlightType.defaultIfNull(flightType)) {
				case ROUND_TRIP -> "1";
				case ONE_WAY -> "2";
				case MULTI_CITY -> "3";
			};
		}

		private String resolveTravelClass(TravelClass travelClass) {
			return TravelClass.defaultIfNull(travelClass).getOptionAsString();
		}
	}
}
