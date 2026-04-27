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
import java.util.function.Function;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Date Type (ADT) modeling a {@link Vehicle} {@literal reservation}.
 *
 * @author John Blum
 * @param vehicle {@link Vehicle} to reserve.
 * @param pickupTime {@link ZonedDateTime} to pickup the vehicle.
 * @param dropOffTime {@link ZonedDateTime} to drop off (return) the vehicle.
 * @param price {@link BigDecimal cost} of the reservation.
 * @see ZonedDateTime
 * @see Vehicle
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record VehicleRental(
	Vehicle vehicle,
	ZonedDateTime pickupTime,
	ZonedDateTime dropOffTime,
	BigDecimal price
) {

	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm Z";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	public static VehicleRental.Builder builder(Vehicle vehicle) {
		return new VehicleRental.Builder(vehicle);
	}

	public VehicleRental {
		Assert.notNull(vehicle, "Vehicle is required");
		assertPickupTime(pickupTime);
		assertDropOffTime(pickupTime, dropOffTime);
	}

	private void assertPickupTime(ZonedDateTime pickup) {

		Assert.notNull(pickup, "Check-in date/time is required");

		Assert.isTrue(pickup.isAfter(ZonedDateTime.now(pickup.getZone())),
			() -> "Pickup date/time [%s] must be after [%s]".formatted(pickup.format(DATE_TIME_FORMATTER),
				ZonedDateTime.now(pickup.getZone()).format(DATE_TIME_FORMATTER)));
	}

	private void assertDropOffTime(ZonedDateTime dropOff, ZonedDateTime pickup) {

		Assert.notNull(dropOff, "Checkout date/time is required");

		Assert.isTrue(dropOff.isAfter(pickup), () -> "Drop-off [%s] must be after pickup [%s]"
			.formatted(dropOff.format(DATE_TIME_FORMATTER), pickup.format(DATE_TIME_FORMATTER)));
	}

	public VehicleReservation reserve(Function<VehicleRental, VehicleReservation> reservationFunction) {
		return reservationFunction.apply(this);
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private BigDecimal price;

		private final Vehicle vehicle;

		private ZonedDateTime dropOff;
		private ZonedDateTime pickup;

		protected Builder(Vehicle vehicle) {
			this.vehicle = ObjectUtils.requireObject(vehicle, "Vehicle is required");
		}

		public Builder droppingOff(ZonedDateTime dropOff) {
			Assert.notNull(dropOff, "Drop-off date/time is reuqired");
			this.dropOff = dropOff;
			return this;
		}

		public Builder pickingUp(ZonedDateTime pickup) {
			Assert.notNull(pickup, "Pickup date/time is required");
			this.pickup = pickup;
			return this;
		}

		public Builder price(BigDecimal price) {
			this.price = price;
			return this;
		}

		public VehicleRental build() {
			return new VehicleRental(getVehicle(), getPickup(), getDropOff(), getPrice());
		}
	}
}
