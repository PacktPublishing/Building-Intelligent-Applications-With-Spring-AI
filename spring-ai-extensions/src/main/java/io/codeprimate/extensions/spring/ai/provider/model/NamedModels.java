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
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.codeprimate.extensions.util.Utils;

import org.cp.elements.util.stream.Streamable;

/**
 * {@link Iterable Collection} of {@link NamedModel Named Models}
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see io.codeprimate.extensions.spring.ai.provider.model.NamedModel
 * @see org.cp.elements.util.stream.Streamable
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface NamedModels extends Iterable<NamedModel>, Streamable<NamedModel> {

	static NamedModels empty() {
		return Collections::emptyIterator;
	}

	static NamedModels of(NamedModel... namedModels) {
		return of(Arrays.asList(namedModels));
	}

	static NamedModels of(Iterable<NamedModel> namedModels) {
		return Utils.nullSafeIterable(namedModels)::iterator;
	}

	default Optional<NamedModel> findBy(Predicate<NamedModel> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default Optional<NamedModel> findByName(String modelName) {
		return findBy(namedModel -> namedModel.getName().equalsIgnoreCase(modelName));
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	@Override
	default Stream<NamedModel> stream() {
		return Utils.stream(this);
	}
}
