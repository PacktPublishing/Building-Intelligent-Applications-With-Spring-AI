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
package com.packt.spring.ai.examples.connect4.support;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of {@literal ASCII} colors.
 *
 * @author John Blum
 * @see java.lang.Enum
 * @since 0.1.0
 */
@Getter
public enum AsciiColors {

	GREEN("\u001b[32m"),
	RED("\u001b[31m"),
	RESET("\u001b[0m"),
	YELLOW("\u001b[33m");

	private final String asciiColorCode;

	AsciiColors(String asciiColorCode) {
		this.asciiColorCode = asciiColorCode;
	}

	public String format(String text) {
		return "%s%s%s".formatted(getAsciiColorCode(), text, RESET);
	}

	@Override
	public String toString() {
		return getAsciiColorCode();
	}
}
