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
package io.codeprimate.extensions.spring.ai.provider.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import io.codeprimate.extensions.data.struct.Collectable;
import io.codeprimate.extensions.util.Utils;

/**
 * {@link Iterable Collection} of {@link NamedModel Named Models}
 *
 * @author John Blum
 * @see Collectable
 * @see NamedModel
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface NamedModels extends Collectable<NamedModel> {

	static NamedModels empty() {
		return Collections::emptyIterator;
	}

	static NamedModels of(NamedModel... namedModels) {
		return of(Arrays.asList(namedModels));
	}

	static NamedModels of(Iterable<NamedModel> namedModels) {
		return Utils.nullSafeIterable(namedModels)::iterator;
	}

	default Optional<NamedModel> findByName(String modelName) {
		return findBy(namedModel -> namedModel.getName().equalsIgnoreCase(modelName));
	}
}
