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
package example.model;

/**
 * Abstract Data Type (ADT) and Java record modeling an {@literal aircraft}, such as an {@literal airplane}.
 *
 * @author John Blum
 * @param make {@link String Make} of the {@literal aircraft}, such as {@literal Boeing}.
 * @param model {@link String Model} of the {@literal aircraft}, such as {@literal 747}.
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Aircraft(String make, String model) {

	/**
	 * Abstract Data Type (ADT) and Java record modeling the seat assignment (location) on an aircraft.
	 *
	 * @param row {@link Integer#TYPE} indicating the row of the aircraft.
	 * @param column {@link Character#TYPE} indicating the seat assignment within the row location (e.g. A-F).
	 * @param classification {@link Classification} of the seat assignment (e.g. First Class).
	 */
	public record Seat(int row, char column, Classification classification) {

		public enum Classification {
			BUSINESS_CLASS, FIRST_CLASS, PREMIUM_ECONOMY, ECONOMY;
		}
	}
}
