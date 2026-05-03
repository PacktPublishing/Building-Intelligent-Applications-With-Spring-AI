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

import java.time.Year;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling a {@literal vehicle}, such as a car, as the mode of transportation.
 *
 * @param year {@link Year} the vehicle was made, e.g. {@literal 2020}.
 * @param make {@link String manufacturer} of the vehicle, e.g. {@literal Audi}.
 * @param model {@link String model number} of the vehicle, e.g. {@literal R8}.
 * @param mileage {@link Integer#TYPE number} of miles on the vehicle.
 * @param type {@link Type} of vehicle.
 * @author John Blum
 * @see Year
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Vehicle(Year year, String make, String model, int mileage, Type type) {

	public Vehicle {
		Assert.hasText(make, "Make is required");
		Assert.hasText(model, "Model is required");
		Assert.isTrue(mileage > -1, "Mileage must be greater than equal to 0");
	}

	public boolean isElectric() {
		return Vehicle.Type.ELECTRIC.equals(type()) || Vehicle.Type.PHEV.equals(type());
	}

	public boolean isNonElectric() {
		return !isElectric();
	}

	public enum Type {
		COMPACT, COUPE, ELECTRIC, LUXURY, MIDSIZE, OVERSIZED, PHEV, SEDAN, SUV, TRUCK, VAN
	}
}
