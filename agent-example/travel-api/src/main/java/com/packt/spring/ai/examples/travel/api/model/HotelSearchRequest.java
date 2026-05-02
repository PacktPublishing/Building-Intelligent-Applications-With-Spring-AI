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

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a search request for {@link Hotel hotels}.
 *
 * @author John Blum
 * @see Hotel
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class HotelSearchRequest {

	protected static final String DATE_PATTERN = "yyyy-MM-dd";
	protected static final String DATE_TIME_PATTERN = DATE_PATTERN.concat(" HH:mm");

	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
	protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	protected static final BigDecimal MAX_PRICE = BigDecimal.valueOf(500.0d);

	public static HotelSearchRequest.CheckInBuilder stayAt(Hotel hotel) {
		return new HotelSearchRequest.Builder(hotel);
	}

	private static String formatDate(ZonedDateTime dateTime) {
		return dateTime.format(DATE_FORMATTER);
	}

	private static String formatDateTime(ZonedDateTime dateTime) {
		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private int occupants = 1;

	private BigDecimal price;

	private final Hotel hotel;

	private final ZonedDateTime checkIn;
	private final ZonedDateTime checkout;

	protected HotelSearchRequest(Hotel hotel, ZonedDateTime checkIn, ZonedDateTime checkout) {
		this.hotel = ObjectUtils.requireObject(hotel, "Hotel is required");
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

	public HotelSearchRequest payLessThan(BigDecimal price) {
		this.price = price;
		return this;
	}

	public HotelSearchRequest withOccupants(int occupants) {
		Assert.isTrue(occupants > 0, "Number of occupants [%d] must be greater than 0", occupants);
		this.occupants = occupants;
		return this;
	}

	public interface CheckInBuilder {
		CheckoutBuilder checkIn(ZonedDateTime checkIn);
	}

	public interface CheckoutBuilder {
		PayBuilder checkout(ZonedDateTime checkout);
	}

	public interface PayBuilder {

		Builder pay(BigDecimal price);

		// Pays any amount up to $500.00 USD
		default Builder payAny() {
			return pay(MAX_PRICE);
		}
	}

	public interface RequestBuilder {

		RequestBuilder withOccupants(int occupants);

		default RequestBuilder oneOccupant() {
			return withOccupants(1);
		}

		default RequestBuilder twoOccupants() {
			return withOccupants(2);
		}

		HotelSearchRequest build();

	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder implements CheckInBuilder, CheckoutBuilder, PayBuilder, RequestBuilder {

		private int occupants = 1;

		private BigDecimal price;

		private final Hotel hotel;

		private ZonedDateTime checkIn;
		private ZonedDateTime checkout;

		protected Builder(Hotel hotel) {
			this.hotel = ObjectUtils.requireObject(hotel, "Hotel is required");
		}

		@Override
		public CheckoutBuilder checkIn(ZonedDateTime checkIn) {
			this.checkIn = ObjectUtils.requireObject(checkIn, "Check-in date/time is required");
			return this;
		}

		@Override
		public PayBuilder checkout(ZonedDateTime checkout) {
			this.checkout = ObjectUtils.requireObject(checkout, "Checkout date/time is required");
			return this;
		}

		@Override
		public Builder pay(BigDecimal price) {
			this.price = price;
			return this;
		}

		@Override
		public RequestBuilder withOccupants(int occupants) {
			Assert.isTrue(occupants > 0, "Number of occupants [%d] must be greater than 0", occupants);
			this.occupants = occupants;
			return this;
		}

		public HotelSearchRequest build() {

			return new HotelSearchRequest(getHotel(), getCheckIn(), getCheckout())
				.withOccupants(getOccupants())
				.payLessThan(getPrice());
		}
	}
}
