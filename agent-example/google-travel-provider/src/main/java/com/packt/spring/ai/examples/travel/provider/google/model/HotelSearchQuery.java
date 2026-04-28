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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.packt.spring.ai.examples.travel.provider.google.config.SerpApiProperties;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) modeling a search query for {@literal Google Hotels} using {@literal SerpApi}.
 *
 * @author John Blum
 * @see HotelSearchResults
 * @see HttpServiceArgumentResolver
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class HotelSearchQuery {

	protected static final String DATE_PATTERN = "yyyy-MM-dd";
	protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm z";

	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
	protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public static CheckInBuilder stayAt(String hotel) {
		return new CheckInBuilder(hotel);
	}

	private static String formatDate(ZonedDateTime dateTime) {
		return dateTime.format(DATE_FORMATTER);
	}

	private static String formatDateTime(ZonedDateTime dateTime) {
		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private BigDecimal maxPrice;

	private HotelClass hotelClass;

	private Integer adults;
	private Integer children;

	private final ZonedDateTime checkIn;
	private final ZonedDateTime checkout;

	// For example: Marriott
	private final String hotel;

	protected HotelSearchQuery(String hotel, ZonedDateTime checkIn, ZonedDateTime checkout) {
		this.hotel = StringUtils.requireText(hotel, "Name of hotel is required");
		this.checkIn = assertCheckIn(checkIn);
		this.checkout = assertCheckout(checkout, checkIn);
	}

	private ZonedDateTime assertCheckIn(ZonedDateTime checkIn) {

		Assert.notNull(checkIn, "Check-in date/time is required");

		Assert.isTrue(checkIn.isAfter(ZonedDateTime.now()),
			() -> "Check-in date/time [%s] must be after [%s]"
				.formatted(formatDateTime(checkIn), formatDateTime(ZonedDateTime.now())));

		return checkIn;
	}

	private ZonedDateTime assertCheckout(ZonedDateTime checkout, ZonedDateTime checkIn) {

		Assert.notNull(checkout, "Checkout date/time is required");

		Assert.isTrue(checkout.isAfter(checkIn),
			() -> "Checkout date/time [%s] must be after Check-in date/time [%s]"
				.formatted(formatDateTime(checkout), formatDateTime(checkIn)));

		return checkout;
	}

	public Integer getAdults() {
		Integer adults = this.adults;
		return adults != null ? adults : 1;
	}

	public Integer getChildren() {
		Integer children = this.children;
		return children != null ? children : 0;
	}

	public BigDecimal getMaxPrice() {
		BigDecimal maxPrice = this.maxPrice;
		return maxPrice != null ? maxPrice : BigDecimal.valueOf(Double.MAX_VALUE);
	}

	public HotelSearchQuery payLessThan(BigDecimal price) {
		this.maxPrice = price;
		return this;
	}

	public HotelSearchQuery withAdults(int adults) {
		this.adults = Math.max(adults, 1);
		return this;
	}

	public HotelSearchQuery withChildren(int children) {
		this.children = Math.max(children, 0);
		return this;
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class CheckInBuilder {

		private final String hotel;

		public CheckoutBuilder checkingIn(ZonedDateTime checkIn) {
			return new CheckoutBuilder(getHotel(), checkIn);
		}
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class CheckoutBuilder {

		private final String hotel;

		private final ZonedDateTime checkIn;

		public QueryBuilder checkingOut(ZonedDateTime checkout) {
			return new QueryBuilder(getHotel(), getCheckIn(), checkout);
		}
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class QueryBuilder {

		private final String hotel;

		private final ZonedDateTime checkIn;
		private final ZonedDateTime checkout;

		public HotelSearchQuery build() {
			return new HotelSearchQuery(getHotel(), getCheckIn(), getCheckout());
		}
	}

	@Getter(AccessLevel.PROTECTED)
	public static class HotelSearchQueryArgumentResolver implements HttpServiceArgumentResolver {

		private final SerpApiProperties properties;

		public HotelSearchQueryArgumentResolver(SerpApiProperties properties) {
			this.properties = ObjectUtils.requireObject(properties, "SerpApiProperties are required");
		}

		@Override
		@SuppressWarnings("all")
		public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {

			if (HotelSearchQuery.class.equals(parameter.getParameterType())) {

				HotelSearchQuery query = (HotelSearchQuery) argument;

				requestValues.addRequestParameter("api_key", getProperties().getApiKey());
				requestValues.addRequestParameter("engine", getProperties().getEngine().getHotels());
				requestValues.addRequestParameter("q", query.getHotel());
				requestValues.addRequestParameter("check_in_date", formatDate(query.getCheckIn()));
				requestValues.addRequestParameter("check_out_date", formatDate(query.getCheckout()));
				requestValues.addRequestParameter("adults", String.valueOf(query.getAdults()));
				requestValues.addRequestParameter("children", String.valueOf(query.getChildren()));
				requestValues.addRequestParameter("max_price", String.valueOf(query.getMaxPrice()));

				return true;
			}

			return false;
		}
	}
}
