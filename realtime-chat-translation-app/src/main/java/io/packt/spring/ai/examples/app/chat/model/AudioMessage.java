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
package io.packt.spring.ai.examples.app.chat.model;

import java.io.IOException;

import io.packt.spring.ai.examples.app.chat.util.ResourceConverter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling audio data.
 *
 * @author John Blum
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AudioMessage {

	static AudioMessage from(byte[] data) {
		Assert.notNull(data, "Audio data is required");
		Assert.isTrue(data.length > 0, "Audio bytes are required");
		return from(new ByteArrayResource(data));
	}

	static AudioMessage from(Resource resource) {
		Assert.notNull(resource, "Resource is required");
		return () -> resource;
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default byte[] getData() {
		return ResourceConverter.INSTANCE.convert(getResource());
	}

	Resource getResource();

	default long size() {
		try {
			return getResource().contentLength();
		}
		catch (IOException ignore) {
			return 0L;
		}
	}
}
