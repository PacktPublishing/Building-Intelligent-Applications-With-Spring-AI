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

import static org.cp.elements.lang.RuntimeExceptionsFactory.newUnsupportedOperationException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.codeprimate.extensions.spring.ai.provider.AiProvider;

import org.cp.elements.lang.Integers;
import org.cp.elements.lang.Nameable;
import org.cp.elements.lang.StringUtils;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a {@link Nameable named} {@link AiProvider AI provider} model.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.cp.elements.lang.Nameable
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface NamedModel extends Nameable<String> {

	int DEFAULT_CONTEXT_WINDOW = 0;

	/**
	 * Factory method that returns a {@link NamedModel.Builder} used to construct a new {@link NamedModel}
	 * with the given {@link String name}.
	 *
	 * @param name {@link String name} of the model; required.
	 * @return a {@link NamedModel.Builder} used to construct a new {@link NamedModel}.
	 * @throws IllegalArgumentException if {@link String name} is {@literal null} or empty.
	 * @see #from(String)
	 */
	static NamedModel.Builder builder(String name) {
		return new NamedModel.Builder(name);
	}

	/**
	 * Factory method used to construct a new {@link NamedModel} from the given {@link String name}.
	 *
	 * @param modelName {@link String name} of the model; required.
	 * @return a new {@link NamedModel} with the given {@link String name}.
	 * @throws IllegalArgumentException if {@link String name} is {@literal null} or empty.
	 * @see #builder(String)
	 */
	static NamedModel from(String modelName) {
		Assert.hasText(modelName, "Name of AI Model is required");
		return () -> modelName;
	}

	default boolean isBase() {
		return isFoundation();
	}

	default int getContextWindow() {
		return DEFAULT_CONTEXT_WINDOW;
	}

	default int getDimensions() {
		throw newUnsupportedOperationException("[%s] is not an Embedding Model".formatted(getName()));
	}

	default boolean isFoundation() {
		return false;
	}

	default Set<Modality> getModalities() {
		return Collections.emptySet();
	}

	default boolean isMultimodal() {
		return getModalities().size() > 1;
	}

	default ModelUnit getUnit() {
		return ModelUnit.TOKEN;
	}

	@Getter(AccessLevel.PROTECTED)
	class Builder implements org.cp.elements.lang.Builder<NamedModel> {

		private boolean foundation;

		private int dimensions;

		private int contextWindow = DEFAULT_CONTEXT_WINDOW;

		private final Set<Modality> modalities = new HashSet<>();

		private final String name;

		protected Builder(String name) {
			this.name = StringUtils.requireText(name, "Name of AI Model is required");
		}

		public Builder asAudio() {
			return asType(Modality.AUDIO);
		}

		public Builder asAudioTranscription() {
			return asType(Modality.AUDIO_TRANSCRIPTION);
		}

		public Builder asChat() {
			return asType(Modality.CHAT);
		}

		public Builder asEmbedding() {
			return asType(Modality.EMBEDDING);
		}

		public Builder asFoundation() {
			this.foundation = true;
			return this;
		}

		public Builder asImage() {
			return asType(Modality.IMAGE);
		}

		public Builder asModeration() {
			return asType(Modality.MODERATION);
		}

		public Builder asText() {
			return asType(Modality.TEXT);
		}

		public Builder asTextToSpeech() {
			return asType(Modality.TEXT_TO_SPEECH);
		}

		public Builder asVideo() {
			return asType(Modality.VIDEO);
		}

		public Builder asType(Modality modelType) {
			Assert.notNull(modelType, "Modality is required");
			this.modalities.add(modelType);
			return this;
		}

		public Builder withContextWindow(int contextWindow) {
			Assert.isTrue(Integers.isGreaterThanZero(contextWindow), "Context window [%d] must be greater than 0");
			this.contextWindow = contextWindow;
			return this;
		}

		public Builder withDimensions(int dimensions) {
			Assert.isTrue(Integers.isGreaterThanZero(dimensions), "Dimensions [%d] for Embedding must be greater than 0");
			this.dimensions = dimensions;
			return this;
		}

		@Override
		public NamedModel build() {

			return new NamedModel() {

				@Override
				public int getContextWindow() {
					return Builder.this.getContextWindow();
				}

				@Override
				public int getDimensions() {
					int dimensions = Builder.this.getDimensions();
					return Integers.isGreaterThanZero(dimensions) ? dimensions
						: NamedModel.super.getDimensions();
				}

				@Override
				public boolean isFoundation() {
					return Builder.this.isFoundation();
				}

				@Override
				public String getName() {
					return Builder.this.getName();
				}

				@Override
				public Set<Modality> getModalities() {
					return Builder.this.getModalities();
				}
			};
		}
	}
}
