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
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) and Java record modeling a {@link Hotel} for {@literal reservation}.
 *
 * @author John Blum
 * @param number Hotel reservation {@link String confirmation number}.
 * @param hotel {@link Hotel} of stay.
 * @param room {@link Hotel.Room} of stay.
 * @param occupants {@link Integer#TYPE number} of occupants staying in the room.
 * @param checkIn {@link ZonedDateTime} of check-in.
 * @param checkout {@link ZonedDateTime} of check-out.
 * @param price {@link BigDecimal} estimating the cost per night for the stay.
 * @see ZonedDateTime
 * @see BigDecimal
 * @see Hotel
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record HotelBooking(
	String number,
	Hotel hotel,
	Hotel.Room room,
	int occupants,
	ZonedDateTime checkIn,
	ZonedDateTime checkout,
	BigDecimal price
) {

	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm Z";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public static HotelBooking.Builder builder(String reservationNumber) {
		return new HotelBooking.Builder(reservationNumber);
	}

	public HotelBooking {
		Assert.hasText(number, "Hotel reservation number is required");
		Assert.notNull(hotel, "Hotel is required");
		Assert.notNull(room, "Hotel Room is required");
		Assert.isTrue(occupants > 0, "Number of occupants must be greater than 0");
		assertCheckIn(checkIn);
		assertCheckout(checkout, checkIn);
	}

	private void assertCheckIn(ZonedDateTime checkIn) {

		Assert.notNull(checkIn, "Check-in date/time is required");

		Assert.isTrue(checkIn.isAfter(ZonedDateTime.now(checkIn.getZone())),
			() -> "Check-in date/time [%s] must be after [%s]".formatted(checkIn.format(DATE_TIME_FORMATTER),
				ZonedDateTime.now(checkIn.getZone()).format(DATE_TIME_FORMATTER)));
	}

	private void assertCheckout(ZonedDateTime checkout, ZonedDateTime checkIn) {

		Assert.notNull(checkout, "Checkout date/time is required");

		Assert.isTrue(checkout.isAfter(checkIn), () -> "Checkout [%s] must be after check-in [%s]"
			.formatted(checkout.format(DATE_TIME_FORMATTER), checkIn.format(DATE_TIME_FORMATTER)));
	}

	@SuppressWarnings("all")
	public int numberOfDays() {
		LocalDate checkIn = checkIn().toLocalDate();
		LocalDate checkout = checkout().toLocalDate();
		int days = Period.between(checkIn, checkout).getDays();
		return days;
	}

	@SuppressWarnings("all")
	public int numberOfNights() {
		int days = numberOfDays();
		int nights = days - 1;
		return nights;
	}

	public HotelReservation reserve(Function<HotelBooking, HotelReservation> reservationFunction) {
		return reservationFunction.apply(this);
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private int occupants;

		private BigDecimal price;

		private Hotel hotel;

		private Hotel.Room room;

		private final String number;

		private ZonedDateTime checkIn;
		private ZonedDateTime checkout;

		protected Builder(String number) {
			this.number = StringUtils.requireText(number, "Hotel reservation number is required");
		}

		public Builder checkingIn(ZonedDateTime checkIn) {
			Assert.notNull(checkIn, "Check-in date/time is required");
			this.checkIn = checkIn;
			return this;
		}

		public Builder checkingOut(ZonedDateTime checkout) {
			Assert.notNull(checkout, "Checkout date/time is required");
			this.checkout = checkout;
			return this;
		}

		public Builder price(BigDecimal price) {
			Assert.notNull(price, "Price is required");
			this.price = price;
			return this;
		}

		public Builder occupiedBy(int occupants) {
			Assert.isTrue(occupants > 0, "Number of occupants [%d] must be greater than 0", occupants);
			this.occupants = occupants;
			return this;
		}

		public Builder stayingAt(Hotel hotel) {
			Assert.notNull(hotel, "Hotel is required");
			this.hotel = hotel;
			return this;
		}

		public Builder inRoom(Hotel.Room room) {
			Assert.notNull(room, "Hotel Room is required");
			this.room = room;
			return this;
		}

		public HotelBooking build() {
			return new HotelBooking(getNumber(), getHotel(), getRoom(), getOccupants(), getCheckIn(), getCheckout(),
				getPrice());
		}
	}
}
