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

import java.util.function.Function;

/**
 * {@link Function} capable of throwing a {@literal checked} {@link Exception} during computation.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.util.function.Function
 * @since 0.1.0
 */
@SuppressWarnings("unused")
@FunctionalInterface
public interface ExceptionThrowingFunction<T, R> {

	static <T, R> Function<T, R> applySafely(ExceptionThrowingFunction<T, R> function) {

		return target -> {
			try {
				return function.apply(target);
			}
			catch (Exception cause) {
				throw new RuntimeException(cause);
			}
		};
	}

	R apply(T  target) throws Exception;

	default <S> ExceptionThrowingFunction<T, S> andThen(ExceptionThrowingFunction<R, S> after) {

		return target -> {
			R processedTarget = this.apply(target);
			return after.apply(processedTarget);
		};
	}
}
