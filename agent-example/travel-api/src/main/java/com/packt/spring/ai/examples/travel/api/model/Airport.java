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

import java.util.Objects;

import org.cp.elements.lang.Nameable;
import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling an {@literal airport}.
 *
 * @author John Blum
 * @see org.cp.elements.lang.Nameable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface Airport extends Nameable<String> {

	static Airport from(String name, String code) {

		Assert.hasText(code, "Airport code is required");
		Assert.hasText(name, "Name of airport is required");

		return new Airport() {

			@Override
			public String getCode() {
				return code;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public boolean equals(Object obj) {

				if (this == obj) {
					return true;
				}

				if (!(obj instanceof Airport that)) {
					return false;
				}

				return this.getCode().equals(that.getCode());
			}

			@Override
			public int hashCode() {
				int hashValue = 31;
				hashValue = 17 * hashValue + Objects.hash(getCode());
				return hashValue;
			}

			@Override
			public String toString() {
				return getCode();

			}
		};
	}

	String getCode();

}
