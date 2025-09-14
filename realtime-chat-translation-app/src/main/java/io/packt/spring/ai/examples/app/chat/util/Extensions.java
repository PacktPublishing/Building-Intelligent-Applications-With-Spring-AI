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
package io.packt.spring.ai.examples.app.chat.util;

import java.util.function.Function;

import org.cp.elements.function.ThrowableSupplier;

/**
 * Common and reusable Java Language API Extensions.
 *
 * @author John Blum
 * @since 0.1.0
 */
abstract class Extensions {

	static final String EMPTY_STRING = "";

	static boolean isNot(boolean condition) {
		return !condition;
	}

	static <T> T getSafely(ThrowableSupplier<T> supplier, Function<Exception, T> exceptionHandler) {
		try {
			return supplier.get();
		}
		catch (Exception cause) {
			return exceptionHandler.apply(cause);
		}
	}
}
