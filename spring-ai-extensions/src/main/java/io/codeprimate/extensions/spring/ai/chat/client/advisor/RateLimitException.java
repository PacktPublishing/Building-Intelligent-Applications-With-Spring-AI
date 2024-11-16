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
package io.codeprimate.extensions.spring.ai.chat.client.advisor;

import java.time.Duration;

/**
 * Java {@link RuntimeException} thrown when the rate limit for AI model interactions has been exceeded.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class RateLimitException extends RuntimeException {

	protected static final String MESSAGE =
		"Exceeded [%d] maximum AI model interactions in [%s ms]; current count is [%d]";

	public static RateLimitException from(int count, Duration duration, int currentCount) {
		return new RateLimitException(MESSAGE.formatted(count, duration.toMillis(), currentCount));
	}

	public RateLimitException() {
	}

	public RateLimitException(String message) {
		super(message);
	}

	public RateLimitException(Throwable cause) {
		super(cause);
	}

	public RateLimitException(String message, Throwable cause) {
		super(message, cause);
	}
}
