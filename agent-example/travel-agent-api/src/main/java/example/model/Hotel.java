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

import org.cp.elements.lang.Nameable;

/**
 * Abstract Data Type (ADT) modeling a {@literal hotel}.
 *
 * @author John Blum
 * @see org.cp.elements.lang.Nameable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface Hotel extends Nameable<String> {

	default String getProviderName() {
		return getName();
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
