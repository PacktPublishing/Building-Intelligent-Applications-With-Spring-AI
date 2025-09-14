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
		return () -> data;
	}

	static AudioMessage from(Resource resource) {
		return from(ResourceConverter.INSTANCE.convert(resource));
	}

	byte[] getData();

	default Resource getResource() {
		return new ByteArrayResource(getData());
	}

	default int size() {
		return getData().length;
	}
}
