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
package com.packt.spring.ai.examples.connect4.model;

import io.codeprimate.extensions.util.AnsiColors;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;

import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a {@literal disc} played by a player in Connect4.
 *
 * @author John Blum
 * @see AnsiColors
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public enum Disc {

	RED("X", AnsiColors.RED),
	GOLD("O", AnsiColors.YELLOW);

	private final AnsiColors color;

	private final String symbol;

	Disc(String symbol, AnsiColors color) {
		this.symbol = StringUtils.requireText(symbol, "Symbol is required");
		this.color = ObjectUtils.requireObject(color, "ANSI Color is required");
	}

	public static boolean exists(Disc disc) {
		return disc != null;
	}

	public static String resolveName(Disc disc, String defaultName) {
		return disc != null ? disc.name() : defaultName;
	}

	public static String resolveSymbol(Disc disc, String defaultSymbol) {
		return disc != null ? disc.getSymbol() : defaultSymbol;
	}

	@Override
	public String toString() {
		return getSymbol();
	}

	public String toColoredString() {
		return getColor().format(toString());
	}
}
