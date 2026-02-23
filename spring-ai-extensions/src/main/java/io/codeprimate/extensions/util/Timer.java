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

/**
 * Interface defining a contract to time operations.
 *
 * @author John Blum
 * @see java.time.Duration
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface Timer<IN, OUT> {

	/**
	 * Get the {@link Duration} of the {@link #run(Object)}.
	 *
	 * @return the {@link Duration} of the {@link #run(Object)}.
	 * @see java.time.Duration
	 * @see #run(Object)
	 */
	Duration getTime();

	/**
	 * Run the operation.
	 *
	 * @param input {@link IN argument} to the operation.
	 * @return {@link OUT value} computed by the operation.
	 */
	OUT run(IN input);

}
