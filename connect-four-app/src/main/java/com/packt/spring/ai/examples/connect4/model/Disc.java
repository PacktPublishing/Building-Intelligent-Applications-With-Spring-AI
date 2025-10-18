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

import com.packt.spring.ai.examples.connect4.AbstractConnectFourApplication;

import org.cp.elements.lang.StringUtils;

import lombok.Getter;

/**
 * The Disc class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Getter
public enum Disc {

	RED("X"), GOLD("O");

	private final String symbol;

	Disc(String symbol) {
		this.symbol = StringUtils.requireText(symbol, "Symbol is required");
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
}
