/*
 *  Copyright 2024 Author or Authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.codeprimate.extensions.util;

/**
 * Java {@link FunctionalInterface} used to supply a value with the possibility of throwing an {@link Exception}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.util.function.Supplier
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ExceptionThrowingSupplier<T> {

	static <T> T getSafely(ExceptionThrowingSupplier<T> supplier) {

		try {
			return supplier.get();
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}

	T get() throws Exception;

}
