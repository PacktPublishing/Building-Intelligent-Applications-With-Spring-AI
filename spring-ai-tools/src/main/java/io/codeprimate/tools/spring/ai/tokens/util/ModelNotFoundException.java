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
package io.codeprimate.tools.spring.ai.tokens.util;

/**
 * Java {@link RuntimeException} thrown when a AI model cannot be found by {@link String name}.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ModelNotFoundException extends RuntimeException {

	public static ModelNotFoundException from(String modelName) {
		return new ModelNotFoundException("Model with name [%s] not found".formatted(modelName));
	}

	public ModelNotFoundException() { }

	public ModelNotFoundException(String message) {
		super(message);
	}

	public ModelNotFoundException(Throwable cause) {
		super(cause);
	}

	public ModelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
