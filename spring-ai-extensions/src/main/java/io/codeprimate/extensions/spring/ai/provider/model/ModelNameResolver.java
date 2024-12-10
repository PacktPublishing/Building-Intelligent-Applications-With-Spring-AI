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

import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.embedding.DocumentEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.Model;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.util.Assert;

/**
 * Interface defining a contract to resolve the Spring AI {@link Model} {@link String name}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.springframework.ai.model.Model
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ModelNameResolver {

	List<String> MODEL_NAME_SUFFIXES = List.of(
		AudioTranscriptionModel.class.getSimpleName(),
		StreamingChatModel.class.getSimpleName(),
		ChatModel.class.getSimpleName(),
		DocumentEmbeddingModel.class.getSimpleName(),
		EmbeddingModel.class.getSimpleName(),
		ImageModel.class.getSimpleName(),
		ModerationModel.class.getSimpleName(),
		SpeechModel.class.getSimpleName(),
		Model.class.getSimpleName()
	);

	static ModelNameResolver defaultModelNameResolver() {

		return model -> {
			Assert.notNull(model, "Model is required");
			String modelSimpleTypeName = model.getClass().getSimpleName();
			for (String modelNameSuffix : MODEL_NAME_SUFFIXES) {
				int index = modelSimpleTypeName.indexOf(modelNameSuffix);
				if (index > -1) {
					return modelSimpleTypeName.substring(index);
				}
			}
			return modelSimpleTypeName;
		};
	}

	String resolveName(Model<?, ?> model);

	interface AudioTranscriptionModel { }

	interface SpeechModel { }

}
