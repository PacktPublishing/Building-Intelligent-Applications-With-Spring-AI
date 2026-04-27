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

import org.cp.elements.lang.StringUtils;

/**
 * {@link Enum Enumeration} of {@link Hotel Hotels}.
 *
 * @author John Blum
 * @see Enum
 * @see Hotel
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum Hotels implements Hotel {

	HILTON,
	HYATT,
	IHG("InterContinental Hotels Group"),
	MARRIOTT,
	WYNDHAM;

	private final String name;

	Hotels() {
		this(null);
	}

	Hotels(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		String resolvedName = StringUtils.hasText(this.name) ? this.name : name();
		return StringUtils.capitalize(resolvedName);
	}
}
