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
package io.codeprimate.extensions.spring.core.io;

import org.cp.elements.lang.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Abstract utility class used to create and process Spring {@link Resource Resources}
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @since 0.1.0
 */
public abstract class ResourceUtils {

	public static Resource newResource(String resourcePath) {
		Assert.hasText(resourcePath, "Resource path [%s] is required", resourcePath);
		Resource resource = new ClassPathResource(resourcePath);
		resource = resource.exists() ? resource : new FileSystemResource(resourcePath);
		Assert.isTrue(resource.exists(), "Resource [%s] not found", resourcePath);
		return resource;
	}

}
