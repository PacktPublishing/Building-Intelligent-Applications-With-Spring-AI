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
package io.packt.spring.ai.examples.app.shazam.support;

import java.time.Duration;

import org.cp.elements.time.DateTimeUtils;

/**
 * Abstract utility class encapsulating common functions for time.
 *
 * @author John Blum
 * @since 0.1.0
 */
public abstract class TimeUtils {

	public static final int MILLISECONDS_PER_SECOND = DateTimeUtils.MILLISECONDS_IN_SECOND;

	public static boolean isZero(Duration duration) {
		return nullSafeDuration(duration).isZero();
	}

	public static boolean isNotZero(Duration duration) {
		return !isZero(duration);
	}

	public static Duration nullSafeDuration(Duration duration) {
		return duration != null ? duration : Duration.ZERO;
	}
}
