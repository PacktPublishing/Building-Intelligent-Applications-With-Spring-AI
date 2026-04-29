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

import org.cp.elements.lang.Nameable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling a {@literal hotel}.
 *
 * @author John Blum
 * @see FunctionalInterface
 * @see org.cp.elements.lang.Nameable
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface Hotel extends Nameable<String> {

	static Hotel from(String providerName) {
		Assert.hasText(providerName, "Hotel provider name is required");
		return () -> providerName;
	}

	default @Nullable Location getLocation() {
		return null;
	}

	default String getProviderName() {
		return getName();
	}

	default Hotel inLocation(Location location) {

		return new Hotel() {

			@Override
			public Location getLocation() {
				return location;
			}

			@Override
			public String getName() {
				return Hotel.this.getName();
			}
		};
	}

	/**
	 * Abstract Data Type (ADT) modeling a {@literal room} at a {@link Hotel}.
	 *
	 * @param number {@link String room number}
	 * @param bedDescription {@link String description} of the number of beds; e.g. 1 king or 2 queens.
	 */
	record Room(int floor, String number, String bedDescription) {

	}
}
