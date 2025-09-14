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
package io.packt.spring.ai.examples.app.chat.util;

import java.io.IOException;

import org.cp.elements.lang.Assert;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Spring {@link Converter} used to convert a {@link MultipartFile} to a {@link Resource}.
 *
 * @author John Blum
 * @see org.springframework.core.convert.converter.Converter
 * @see org.springframework.core.io.Resource
 * @see org.springframework.web.multipart.MultipartFile
 * @since 0.1.0
 */
public class MultipartFileConverter implements Converter<MultipartFile, Resource> {

	public static final MultipartFileConverter INSTANCE = new MultipartFileConverter();

	@Override
	public @NonNull Resource convert(@NonNull MultipartFile file) {

		Assert.notNull(file, "MultipartFile to convert into a Resource is required");

		try {
			return new ByteArrayResource(file.getBytes());
		}
		catch (IOException cause) {
			throw new ConversionFailedException(TypeDescriptor.forObject(file),
				TypeDescriptor.valueOf(MultipartFile.class), file, cause);
		}
	}
}
