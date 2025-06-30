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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import io.codeprimate.extensions.spring.ai.provider.model.NamedModel;
import io.codeprimate.extensions.spring.ai.provider.model.NamedModels;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Nameable;
import org.cp.elements.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling, identifying and classifying AI providers.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see java.lang.FunctionalInterface
 * @see org.cp.elements.lang.Nameable
 * @see io.codeprimate.extensions.spring.ai.provider.model.NamedModel
 * @see io.codeprimate.extensions.spring.ai.provider.model.NamedModels
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface AiProvider extends Iterable<NamedModel>, Nameable<String> {

	/**
	 * Factory method that returns a {@link AiProvider.Builder} used construct a new {@link AiProvider}
	 * with the given {@link String name}.
	 *
	 * @param name {@link String name} of the {@link AiProvider AI provider}; required.
	 * @return a new {@link AiProvider.Builder} used to construct a new {@link AiProvider}.
	 * @throws IllegalArgumentException if {@link String name} is {@literal null} or empty.
	 * @see #from(String)
	 */
	static AiProvider.Builder builder(String name) {
		return new AiProvider.Builder(name);
	}

	/**
	 * Factory method used to construct a new {@link AiProvider} with the given {@link String name}.
	 *
	 * @param name {@link String name} of the given {@link AiProvider AI provider}; required.
	 * @return a new {@link AiProvider} with the given {@link String name}.
	 * @throws IllegalArgumentException if {@link String name} is {@literal null} or empty.
	 * @see #builder(String)
	 */
	static AiProvider from(String name) {
		Assert.hasText(name, "Name of AI provider is required");
		return () -> name;
	}

	@Override
	@SuppressWarnings("all")
	default Iterator<NamedModel> iterator() {
		return namedModels().iterator();
	}

	/**
	 * Return an {@link Iterable} of {@link NamedModel named models} provided by this {@link AiProvider AI provider}.
	 *
	 * @return an {@link Iterable} of {@link NamedModel named models} provided by this {@link AiProvider AI provider}.
	 * @see io.codeprimate.extensions.spring.ai.provider.model.NamedModel
	 * @see java.lang.Iterable
	 */
	default Iterable<NamedModel> namedModels() {
		return NamedModels.empty();
	}

	/**
	 * Elements {@link org.cp.elements.lang.Builder} used to construct a new {@link AiProvider}.
	 *
	 * @see org.cp.elements.lang.Builder
	 */
	@Getter(AccessLevel.PROTECTED)
	class Builder implements org.cp.elements.lang.Builder<AiProvider> {

		private final String name;

		private final Set<NamedModel> namedModels = new HashSet<>();

		protected Builder(String name) {
			this.name = StringUtils.requireText(name, "Name of AI provider is required");
		}

		public Builder withNamedModel(NamedModel namedModel) {
			return withNamedModels(namedModel);
		}

		public Builder withNamedModels(NamedModel... namedModels) {
			return withNamedModels(Arrays.asList(namedModels));
		}

		public Builder withNamedModels(Iterable<NamedModel> namedModels) {

			Utils.stream(namedModels)
				.filter(Objects::nonNull)
				.forEach(this.namedModels::add);

			return this;
		}

		@Override
		public AiProvider build() {

			NamedModels namedModels = NamedModels.of(getNamedModels());

			return new AiProvider() {

				@Override
				public String getName() {
					return Builder.this.getName();
				}

				@Override
				public Iterable<NamedModel> namedModels() {
					return namedModels;
				}
			};
		}
	}
}
