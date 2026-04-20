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

import org.cp.elements.lang.StringUtils;

/**
 * {@link Enum Enumeration} of {@link Airport Airports}
 *
 * @author John Blum
 * @see Airport
 * @see Enum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum Airports implements Airport {

	JFK("New York Kennedy"),
	LAX("Los Angeles"),
	LGA("New York La Guardia"),
	ORD("Chicago O'Hare"),
	PDX("Portland International Airport"),
	PHX("Phoenix Sky Harbor International"),
	SAN("San Diego"),
	SEA("Seattle/Tacoma International"),
	SFO("San Francisco"),
	SJC("San Jose");

	private final String name;

	Airports(String name) {
		this.name = StringUtils.requireText(name, "Name is required");
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getName() {
		return this.name;
	}
}
