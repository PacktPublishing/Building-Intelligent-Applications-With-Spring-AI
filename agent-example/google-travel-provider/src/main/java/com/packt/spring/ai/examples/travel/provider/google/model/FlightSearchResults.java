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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

import io.codeprimate.extensions.data.struct.Collectable;

import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.CollectionUtils;
import org.springframework.lang.NonNull;

import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling results from a flight search query
 * returned by {@literal Google Flights} using the {@literal SerpApi}.
 *
 * @author John Blum
 * @see Collectable
 * @since 0.1.0
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class FlightSearchResults implements Collectable<FlightSearchResults.BestFlight> {

	@JsonProperty("best_flights")
	private List<BestFlight> bestFlights;

	@JsonProperty("price_insights")
	private PriceInsights priceInsights;

	@Override
	public @NonNull Iterator<BestFlight> iterator() {
		return CollectionUtils.unmodifiableIterator(getBestFlights().iterator());
	}

	public List<com.packt.spring.ai.examples.travel.api.model.Flight> toFlights() {

		List<com.packt.spring.ai.examples.travel.api.model.Flight> flights = new ArrayList<>(Long.valueOf(size()).intValue());

		for (BestFlight bestFlight : this) {
			for (Flight flight : bestFlight) {
				com.packt.spring.ai.examples.travel.api.model.Flight resolvedFlight =
					com.packt.spring.ai.examples.travel.api.model.Flight.builder(flight.getFlightNumber())
						.from(flight.getDeparture().toResolvedAirport())
						.departingOn(flight.getDeparture().getZonedTime())
						.to(flight.getDeparture().toResolvedAirport())
						.arrivingOn(flight.getArrival().getZonedTime())
						.flownBy(flight.resolveAirline())
						.flying(flight.resolveAircraft())
						.price(getPriceInsights().getPrice())
						.build();
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
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
		private LocalDateTime time;

		public ZonedDateTime getZonedTime() {
			return ZonedDateTime.from(getTime());
		}

		public com.packt.spring.ai.examples.travel.api.model.Airport toResolvedAirport() {

			return new com.packt.spring.ai.examples.travel.api.model.Airport() {

				@Override
				public String getCode() {
					return getId();
				}

				@Override
				public String getName() {
					return Airport.this.getName();
				}
			};
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BestFlight implements Iterable<Flight> {

		@JsonProperty("total_duration")
		@JsonDeserialize(using = DurationDeserializer.class)
		private Duration totalDuration;

		@JsonProperty("price")
		private Integer price;

		@JsonProperty("flights")
		private List<Flight> flights;

		@JsonProperty("layovers")
		private List<Layover> layovers;

		@Override
		public @NonNull Iterator<Flight> iterator() {
			return CollectionUtils.unmodifiableIterator(getFlights().iterator());
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Flight {

		@JsonProperty("arrival_airport")
		private Airport arrival;

		@JsonProperty("departure_airport")
		private Airport departure;

		@JsonDeserialize(using = FlightDurationDeserializer.class)
		private Duration flightTime;

		@JsonProperty("airline")
		private String airline;

		@JsonProperty("airplane")
		private String airplane;

		@JsonProperty("flight_number")
		private String flightNumber;

		@JsonProperty("travel_class")
		@JsonDeserialize(using = TravelClassDeserializer.class)
		private TravelClass travelClass;

		public Aircraft resolveAircraft() {
			String airplane = getAirplane();
			return new Aircraft(airplane, airplane);
		}

		public Airline resolveAirline() {

			return new Airline() {

				@Override
				public String getCarrierCode() {
					return "";
				}

				@Override
				public String getName() {
					return getAirline();
				}
			};
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

	public static class TravelClassDeserializer extends JsonDeserializer<TravelClass> {

		@Override
		public TravelClass deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
			return TravelClass.from(StringUtils.trim(jsonParser.getText()));
		}
	}
}
