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

/**
 * Abstract Data Type (ADT) and Java Record modeling a vehicle reservation.
 *
 * @author John Blum
 * @param vehicleRental {@link VehicleRental}
 * @param confirmationNumber {@link String confirmation number} for the reservation.
 * @see Confirmation
 * @see VehicleRental
 * @since 0.1.0
 */
public record VehicleReservation(VehicleRental vehicleRental, String confirmationNumber)
		implements Confirmation<String> {

	@Override
	public String getNumber() {
		return confirmationNumber();
	}
}
