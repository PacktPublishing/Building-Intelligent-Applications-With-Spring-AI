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
package io.codeprimate.extensions.spring.ai.provider;

import io.codeprimate.extensions.util.Utils;

import org.springframework.ai.model.Model;

/**
 * Java {@link RuntimeException} throws when an {@link AiProvider AI provider} cannot be found.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class AiProviderNotFoundException extends RuntimeException {

	public static AiProviderNotFoundException from(Model<?, ?> model) {
		return new AiProviderNotFoundException("AI provider for Model [%s] was not found"
			.formatted(Utils.nullSafeTypeName(model)));
	}

	public AiProviderNotFoundException() {
	}

	public AiProviderNotFoundException(String message) {
		super(message);
	}

	public AiProviderNotFoundException(Throwable cause) {
		super(cause);
	}

	public AiProviderNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
