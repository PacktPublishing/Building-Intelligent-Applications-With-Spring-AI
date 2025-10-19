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
package io.codeprimate.extensions.util;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of {@literal ANSI} colors.
 *
 * @author John Blum
 * @see java.lang.Enum
 * @since 0.1.0
 */
@Getter
public enum AnsiColors {

	BLACK("\u001b[30m"),
	BLACK_BACKGROUND("\u001b[40m"),
	BLUE("\u001b[34m"),
	BLUE_BACKGROUND("\u001b[44m"),
	CYAN("\u001b[36m"),
	CYAN_BACKGROUND("\u001b[46m"),
	GREEN("\u001b[32m"),
	GREEN_BACKGROUND("\u001b[42m"),
	MAGENTA("\u001b[35m"),
	MAGENTA_BACKGROUND("\u001b[45m"),
	RED("\u001b[31m"),
	RED_BACKGROUND("\u001b[41m"),
	RESET("\u001b[0m"),
	WHITE("\u001b[37m"),
	WHITE_BACKGROUND("\u001b[47m"),
	YELLOW("\u001b[33m"),
	YELLOW_BACKGROUND("\u001b[43m");

	private final String asciiColorCode;

	AnsiColors(String asciiColorCode) {
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
