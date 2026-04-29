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

import static org.cp.elements.lang.LangExtensions.is;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.packt.spring.ai.examples.travel.api.model.GpsCoordinates;
import com.packt.spring.ai.examples.travel.api.model.HotelBooking;
import com.packt.spring.ai.examples.travel.api.model.Location;

import io.codeprimate.extensions.data.struct.Collectable;

import org.cp.elements.lang.ImmutableIdentifiable;
import org.cp.elements.lang.Nameable;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling search results from a {@link HotelSearchQuery}
 * returned by {@literal Google Hotels} using {@literal SerpApi}.
 *
 * @author John Blum
 * @see Collectable
 * @see HotelBooking
 * @see HotelSearchQuery
 * @see HotelSearchResults.Property
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class HotelSearchResults implements Collectable<HotelSearchResults.Property> {

	@JsonProperty("ads")
	private List<HotelAd> hotelAds = new ArrayList<>();

	@JsonProperty("brands")
	private List<HotelBrand> hotelBrands = new ArrayList<>();

	@JsonProperty("properties")
	private List<Property> properties = new ArrayList<>();

	@JsonProperty("search_metadata")
	private SearchMetadata searchMetadata;

	@JsonProperty("search_parameters")
	private SearchParameters searchParameters;

	@Override
	public @NonNull Iterator<Property> iterator() {
		Iterator<Property> propertiesIterator = getProperties().iterator();
		return CollectionUtils.unmodifiableIterator(propertiesIterator);
	}

	public List<HotelBooking> toHotelBookings() {

		List<HotelBooking> hotelBookings = new ArrayList<>();

		for (Property property : this) {
			if (propertyPredicate().test(property)) {

				HotelBooking hotelBooking = HotelBooking.builder(property.getToken())
					.stayingAt(resolveHotel(property))
					.checkingIn(resolveCheckIn(property))
					.checkingOut(resolveCheckout(property))
					.occupiedBy(resolveOccupants())
					.price(resolvePrice(property))
					.build();

				hotelBookings.add(hotelBooking);
			}
		}

		return hotelBookings;
	}

	private Predicate<Property> propertyPredicate() {

		Predicate<Property> propertyPredicate = property -> true;

		// Filter by Price
		propertyPredicate = propertyPredicate.and(property -> {
			BigDecimal searchPrice = getSearchParameters().getPrice();
			BigDecimal propertyPrice = resolvePrice(property);
			return is(propertyPrice).lessThan(searchPrice);
		});

		// Filter by Type
		propertyPredicate = propertyPredicate.and(property -> {
			String propertyTypeValue = property.getType();
			PropertyType propertyType = PropertyType.from(propertyTypeValue);
			return propertyType.isHotel();
		});

		return propertyPredicate;
	}

	private ZonedDateTime resolveCheckIn(Property property) {
		return resolveDateTime(getSearchParameters()::getCheckIn, property::getCheckIn);
	}

	private ZonedDateTime resolveCheckout(Property property) {
		return resolveDateTime(getSearchParameters()::getCheckout, property::getCheckout);
	}

	private ZonedDateTime resolveDateTime(Supplier<LocalDate> dateSupplier, Supplier<LocalTime> timeSupplier) {

		LocalDate date = dateSupplier.get();
		LocalTime time = timeSupplier.get();
		ZoneId zone = ZoneId.systemDefault();

		return time != null
			? date.atTime(time).atZone(zone)
			: date.atStartOfDay(zone);
	}

	private com.packt.spring.ai.examples.travel.api.model.Hotel resolveHotel(Property property) {

		String propertyName = property.getName();
		Location location = Location.from(property.getGpsCoordinates());

		return com.packt.spring.ai.examples.travel.api.model.Hotel.from(propertyName).inLocation(location);
	}

	@SuppressWarnings("all")
	private int resolveOccupants() {
		int adults = getSearchParameters().getAdults();
		int children = getSearchParameters().getChildren();
		int occupants = adults + children;
		return occupants;
	}

	private BigDecimal resolvePrice(Property property) {
		return property.getRatePerNight().getPrice();
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Hotel implements ImmutableIdentifiable<Long>, Nameable<String> {

		@JsonProperty("id")
		public Long id;

		@JsonProperty(value = "name", required = true)
		public String name;

		@Override
		public String toString() {
			return getName();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HotelBrand implements Nameable<String> {

		@JsonProperty(value = "name", required = true)
		private String name;

		@JsonProperty("children")
		private List<Hotel> hotels;

		@Override
		public String toString() {
			return getName();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HotelAd implements Nameable<String> {

		@JsonProperty("free_cancellation")
		private Boolean freeCancellation;

		@JsonProperty("extracted_price")
		private BigDecimal price;

		@JsonProperty("overall_rating")
		private Double overallRating;

		@JsonProperty("gps_coordinates")
		private GpsCoordinates gpsCoordinates;

		@JsonProperty("hotel_class")
		@JsonDeserialize(using = HotelClassDeserializer.class)
		private HotelClass hotelClass;

		@JsonProperty("reviews")
		private Integer reviews;

		@JsonProperty("amenities")
		private List<String> amenities;

		@JsonProperty(value = "name", required = true)
		private String name;

		@JsonProperty("property_token")
		private String propertyToken;

		@JsonProperty("source")
		private String source;

		@JsonProperty("link")
		private URL link;

		@JsonProperty("thumbnail")
		private URL thumbnail;

		@Override
		public String toString() {
			return getName();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Property implements Nameable<String> {

		protected static final String TIME_PATTERN = "h:mm a";

		@JsonProperty("location_rating")
		private Double locationRating;

		@JsonProperty("overall_rating")
		private Double rating;

		@JsonProperty("gps_coordinates")
		private GpsCoordinates gpsCoordinates;

		@JsonProperty("reviews")
		private Integer reviews;

		@JsonProperty("images")
		private List<Image> images = new ArrayList<>();

		@JsonProperty("amenities")
		private List<String> amenities = new ArrayList<>();

		@JsonProperty("essential_info")
		private List<String> essentialInfo = new ArrayList<>();

		@JsonProperty("excluded_amenities")
		private List<String> excludedAmenities = new ArrayList<>();

		@JsonProperty("places")
		private List<Place> places = new ArrayList<>();

		@JsonProperty("prices")
		private List<Price> prices = new ArrayList<>();

		@JsonProperty("check_in_time")
		//@JsonFormat(pattern = TIME_PATTERN)
		@JsonDeserialize(using = TimeDeserializer.class)
		private LocalTime checkIn;

		@JsonProperty("check_out_time")
		//@JsonFormat(pattern = TIME_PATTERN)
		@JsonDeserialize(using = TimeDeserializer.class)
		private LocalTime checkout;

		@JsonProperty("rate_per_night")
		private Rate ratePerNight;

		@JsonProperty("total_rate")
		private Rate totalRate;

		@JsonProperty("description")
		private String description;

		@JsonProperty(value = "name", required = true)
		private String name;

		@JsonProperty("property_token")
		private String token;

		@JsonProperty("type")
		private String type;

		@Override
		public String toString() {
			return getName();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Image {

		@JsonProperty(value = "original_image", required = true)
		private URL originalImage;

		@JsonProperty("thumbnail")
		private URL thumbnail;

		@Override
		public String toString() {
			return getOriginalImage().toExternalForm();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Place implements Nameable<String> {

		@JsonProperty(value = "name", required = true)
		private String name;

		@JsonProperty("transportations")
		private List<Transportation> transportations = new ArrayList<>();

		@Override
		public String toString() {
			return getName();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Price implements Comparable<Price> {

		@JsonProperty("num_guests")
		private Integer numberOfGuests;

		@JsonProperty(value = "rate_per_night", required = true)
		private Rate ratePerNight;

		@JsonProperty("source")
		private String source;

		@Override
		public int compareTo(Price other) {
			return this.getRatePerNight().compareTo(other.getRatePerNight());
		}

		@Override
		public String toString() {
			return NumberFormat.getCurrencyInstance().format(getRatePerNight().getPrice());
		}
	}

	enum PropertyType {

		HOTEL, UNKNOWN, VACATION_RENTAL;

		static final PropertyType DEFAULT = UNKNOWN;

		static PropertyType from(@Nullable String value) {

			String resolvedValue = String.valueOf(value).replaceAll("\\s+", StringUtils.SINGLE_SPACE);

			return Arrays.stream(values())
				.filter(it -> it.name().equalsIgnoreCase(resolvedValue))
				.findFirst()
				.orElse(DEFAULT);
		}

		boolean isHotel() {
			return this.equals(HOTEL);
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Rate implements Comparable<Rate> {

		@JsonProperty(value = "extracted_lowest", required = true)
		private BigDecimal price;

		@Override
		public int compareTo(Rate other) {
			return this.getPrice().compareTo(other.getPrice());
		}

		@Override
		public String toString() {
			return NumberFormat.getCurrencyInstance().format(getPrice());
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Transportation {

		@JsonProperty("duration")
		private String duration;

		@JsonProperty("type")
		private String type;

		@Override
		public String toString() {
			return getType();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SearchMetadata implements ImmutableIdentifiable<String> {

		protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

		@JsonProperty("created_at")
		@JsonFormat(pattern = DATE_TIME_PATTERN)
		private LocalDateTime createdAt;

		@JsonProperty("processed_at")
		@JsonFormat(pattern = DATE_TIME_PATTERN)
		private LocalDateTime processedAt;

		@JsonProperty("id")
		private String id;

		@JsonProperty("status")
		private String status;

		@JsonProperty("total_time_taken")
		private TotalTimeTaken time;

		@JsonProperty("json_endpoint")
		private URL jsonEndpoint;

		@JsonProperty("google_hotels_url")
		private URL googleHotelsUrl;

		@Override
		public String toString() {
			return getId();
		}
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SearchParameters {

		protected static final String DATE_PATTERN = "yyyy-MM-dd";

		@JsonProperty("max_price")
		private BigDecimal price;

		@JsonProperty("adults")
		private Integer adults;

		@JsonProperty("children")
		private Integer children;

		@JsonProperty("check_in_date")
		@JsonFormat(pattern = DATE_PATTERN)
		private LocalDate checkIn;

		@JsonProperty("check_out_date")
		@JsonFormat(pattern = DATE_PATTERN)
		private LocalDate checkout;

		@JsonProperty("currency")
		private String currency;

		@JsonProperty("engine")
		private String engine;

		@JsonProperty("q")
		private String query;

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TotalTimeTaken {

		@JsonProperty("float")
		private Double time;

	}

	protected static class HotelClassDeserializer extends JsonDeserializer<HotelClass> {

		@Override
		public HotelClass deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
			String value = jsonParser.getText().trim();
			int rating = Integer.parseInt(value);
			return HotelClass.from(rating);
		}
	}

	protected static class TimeDeserializer extends JsonDeserializer<LocalTime> {

		protected static final String TIME_PATTERN = "h:mm a";

		protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

		@Override
		public LocalTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
			String value = jsonParser.getText().trim();
			value = value.replaceAll("(?U)\\s", " ");
			return LocalTime.parse(value, TIME_FORMATTER);
		}
	}
}
