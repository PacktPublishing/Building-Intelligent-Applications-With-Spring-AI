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

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.cp.elements.lang.Assert;

/**
 * Abstract base class encapsulating functionality common to all {@link Timer} implementations.
 *
 * @author John Blum
 * @see Timer
 * @see java.time.Duration
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractTimer<IN, OUT> implements Timer<IN, OUT> {

	public static <IN, OUT> Timer<IN, OUT> time(Consumer<IN> consumer) {

		Assert.notNull(consumer, "Consumer is required");

		return new AbstractTimer<>() {

			@Override
			public OUT run(IN input) {
				return timedRun(() -> {
					consumer.accept(input);
					return null;
				});
			}
		};
	}

	public static <IN, OUT> Timer<IN, OUT> time(Function<IN, OUT> function) {

		Assert.notNull(function, "Function is required");

		return new AbstractTimer<>() {

			@Override
			public OUT run(IN input) {
				return timedRun(() -> function.apply(input));
			}
		};
	}

	public static <IN, OUT> AbstractTimer<IN, OUT> time(Runnable runnable) {

		Assert.notNull(runnable, "Runnable is required");

		return new AbstractTimer<>() {

			@Override
			public OUT run(IN input) {
				return timedRun(() -> {
					runnable.run();
					return null;
				});
			}
		};
	}

	public static <IN, OUT> AbstractTimer<IN, OUT> time(Supplier<OUT> supplier) {

		Assert.notNull(supplier, "Supplier is required");

		return new AbstractTimer<>() {

			@Override
			public OUT run(IN input) {
				return timedRun(supplier);
			}
		};
	}

	private Duration time = Duration.ZERO;

	@Override
	public Duration getTime() {
		return this.time;
	}

	protected OUT timedRun(Supplier<OUT> supplier) {
		long start = System.currentTimeMillis();
		OUT output = supplier.get();
		long end = System.currentTimeMillis();
		this.time = Duration.ofMillis(end - start);
		return output;
	}
}
