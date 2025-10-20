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
package com.packt.spring.ai.examples.connect4.support;

/**
 * Java {@link RuntimeException} thrown when an error occurs playing Connect4.
 *
 * @author John Blum
 * @see RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ConnectFourException extends RuntimeException {

	public static ConnectFourException because(String message, Throwable cause) {
		return new ConnectFourException(message, cause);
	}

	public ConnectFourException() {

	}

	public ConnectFourException(String message) {
		super(message);
	}

	public ConnectFourException(Throwable cause) {
		super(cause);
	}

	public ConnectFourException(String message, Throwable cause) {
		super(message, cause);
	}
}
