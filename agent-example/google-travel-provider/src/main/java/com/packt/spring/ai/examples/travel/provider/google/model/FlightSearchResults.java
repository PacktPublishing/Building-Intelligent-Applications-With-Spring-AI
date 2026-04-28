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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import com.packt.spring.ai.examples.travel.api.model.Aircraft;
import com.packt.spring.ai.examples.travel.api.model.Airline;
import com.packt.spring.ai.examples.travel.api.model.FlightType;

import io.codeprimate.extensions.data.struct.Collectable;

import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.CollectionUtils;
import org.springframework.lang.NonNull;

import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling results from a {@link FlightSearchQuery}
 * returned by {@literal Google Flights} using the {@literal SerpApi}.
 *
 * @author John Blum
 * @see Collectable
 * @see FlightSearchQuery
 * @see com.packt.spring.ai.examples.travel.api.model.Flight
 * @since 0.1.0
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class FlightSearchResults implements Collectable<FlightSearchResults.FlightContainer> {

	protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

	@JsonProperty("best_flights")
	private List<FlightContainer> bestFlights;

	@JsonProperty("other_flights")
	private List<FlightContainer> otherFlights;

	@JsonProperty("price_insights")
	private PriceInsights priceInsights;

	public List<FlightContainer> getBestFlights() {
		return CollectionUtils.nullSafeList(this.bestFlights);
	}

	public List<FlightContainer> getOtherFlights() {
		return CollectionUtils.nullSafeList(this.otherFlights);
	}

	@Override
	public @NonNull Iterator<FlightContainer> iterator() {
		List<FlightContainer> flights = new ArrayList<>(getBestFlights());
		flights.addAll(getOtherFlights());
		Collections.sort(flights);
		return flights.iterator();
	}

	public List<com.packt.spring.ai.examples.travel.api.model.Flight> toFlights() {

		int size = Long.valueOf(size()).intValue();

		List<com.packt.spring.ai.examples.travel.api.model.Flight> flights = new ArrayList<>(size);

		for (FlightContainer flightContainer : this) {
			for (Flight flight : flightContainer) {

				com.packt.spring.ai.examples.travel.api.model.Flight.Builder flightBuilder =
					com.packt.spring.ai.examples.travel.api.model.Flight.builder(flight.getNumber())
						.from(flight.getDeparture().resolveAirport())
						.departOn(flight.getDeparture().getZonedDateTime())
						.to(flight.getArrival().resolveAirport())
						.arriveOn(flight.getArrival().getZonedDateTime())
						.flownBy(flight.resolveAirline())
						.flying(flight.resolveAircraft())
						.price(flightContainer.getPricesAsBigDecimal());

				if (flightContainer.isNonStop()) {
					flightBuilder.duration(flightContainer.getTotalDuration());
				}

				com.packt.spring.ai.examples.travel.api.model.Flight resolvedFlight = flightBuilder.build();

				flights.add(resolvedFlight);
			}
		}

		return flights;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Airport {

		@JsonProperty("id")
		private String id;

		@JsonProperty("name")
		private String name;

		@JsonProperty("time")
		@JsonFormat(pattern = DATE_TIME_PATTERN)
		private LocalDateTime dateTime;

		public ZonedDateTime getZonedDateTime() {
			return getDateTime().atZone(ZoneId.systemDefault());
		}

		public com.packt.spring.ai.examples.travel.api.model.Airport resolveAirport() {
			return com.packt.spring.ai.examples.travel.api.model.Airport.from(getName(), getId());
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AirportLocation {

		@JsonProperty("airport")
		private Airport airport;

		@JsonProperty("city")
		private String city;

		@JsonProperty("country")
		private String country;

		@JsonProperty("country_code")
		private String countryCode;

	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FlightContainer implements Collectable<Flight>, Comparable<FlightContainer> {

		@JsonProperty("total_duration")
		@JsonDeserialize(using = DurationDeserializer.class)
		private Duration totalDuration;

		@JsonProperty("type")
		@JsonDeserialize(using = FlightTypeDeserializer.class)
		private FlightType flightType;

		@JsonProperty("price")
		private Integer price;

		@JsonProperty("flights")
		private List<Flight> flights = new ArrayList<>();

		@JsonProperty("layovers")
		private List<Layover> layovers = new ArrayList<>();

		@JsonProperty("departure_token")
		private String departureToken;

		@JsonProperty("airline_logo")
		private URL airlineLogo;

		public Flight getFirstFlight() {
			return this.flights.get(0);
		}

		public Flight getLastFight() {
			int size = Long.valueOf(size()).intValue();
			int index = size - 1;
			return this.flights.get(index);
		}

		public Flight getNonStopFlight() {
			return getFirstFlight();
		}

		public BigDecimal getPricesAsBigDecimal() {
			return BigDecimal.valueOf(getPrice());
		}

		public boolean hasLayover() {
			return !isNonStop();
		}

		public boolean isNonStop() {
			return getStops() == 0;
		}

		public int getStops() {
			return this.layovers == null ? 0 : this.layovers.size();
		}

		@Override
		public int compareTo(@NonNull FlightContainer other) {

			return Comparator.comparing(FlightContainer::getPrice)
				.thenComparing(FlightContainer::getStops)
				.compare(this, other);
		}

		@Override
		public @NonNull Iterator<Flight> iterator() {
			Iterator<Flight> flightsIterator = getFlights().iterator();
			return CollectionUtils.unmodifiableIterator(flightsIterator);
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Flight {

		@JsonProperty("arrival_airport")
		private Airport arrival;

		@JsonProperty("departure_airport")
		private Airport departure;

		@JsonProperty("overnight")
		private Boolean overnight;

		@JsonProperty("duration")
		@JsonDeserialize(using = FlightDurationDeserializer.class)
		private Duration duration;

		@JsonProperty("airline")
		private String airline;

		@JsonProperty("airplane")
		private String airplane;

		@JsonProperty("flight_number")
		private String number;

		@JsonProperty("travel_class")
		@JsonDeserialize(using = TravelClassDeserializer.class)
		private TravelClass travelClass;

		public boolean isOvernight() {
			return Boolean.TRUE.equals(this.overnight);
		}

		public Aircraft resolveAircraft() {
			String airplane = getAirplane();
			String[] makeModel = airplane.split("\\s+");
			String make = makeModel[0];
			String model = makeModel.length > 1 ? makeModel[1] : make;
			return new Aircraft(make, model);
		}

		public Airline resolveAirline() {
			return Airline.from(getAirline());
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Layover {

		@JsonProperty("duration")
		@JsonDeserialize(using = DurationDeserializer.class)
		private Duration duration;

		@JsonProperty("id")
		private String id;

		@JsonProperty("name")
		private String name;

	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PriceInsights {

		@JsonProperty("lowest_price")
		private BigDecimal price;

	}

	public static class FlightDurationDeserializer extends JsonDeserializer<Duration> {

		@Override
		public Duration deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
			int minutes = Integer.parseInt(jsonParser.getText().trim());
			return Duration.ofMinutes(minutes);
		}
	}

	public static class FlightTypeDeserializer extends JsonDeserializer<FlightType> {

		@Override
		public FlightType deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

			String flightTypeValue = String.valueOf(jsonParser.getText());
			String resolvedFlightTypeValue = flightTypeValue.trim().replaceAll(" ", "_").toUpperCase();

			return FlightType.from(resolvedFlightTypeValue);
		}
	}

	public static class TravelClassDeserializer extends JsonDeserializer<TravelClass> {

		@Override
		public TravelClass deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
			return TravelClass.from(StringUtils.trim(jsonParser.getText()));
		}
	}
}
