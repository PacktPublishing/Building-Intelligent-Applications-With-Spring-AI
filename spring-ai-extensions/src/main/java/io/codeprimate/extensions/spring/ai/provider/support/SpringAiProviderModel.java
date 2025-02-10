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
package io.codeprimate.extensions.spring.ai.provider.support;

import io.codeprimate.extensions.spring.ai.chat.model.ChatModelWrapper;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.AiProviderNotFoundException;
import io.codeprimate.extensions.spring.ai.provider.model.ModelNameResolver;

import org.cp.elements.lang.Assert;
import org.springframework.ai.model.Model;

/**
 * Java Record used to model a {@link AiProvider} and the provider (Spring AI-based) {@link Model}.
 *
 * @author John Blum
 * @param aiProvider {@link AiProvider} to associate with its {@link Model}.
 * @param model {@link Model} provided by the given {@link AiProvider}.
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see org.springframework.ai.model.Model
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record SpringAiProviderModel(SpringAiProvider aiProvider, Model<?, ?> model) implements AiProvider {

	public static SpringAiProviderModel from(Model<?, ?> model) {

		Model<?, ?> resolvedModel = resolveModel(model);

		SpringAiProvider aiProvider = (SpringAiProvider) SpringAiProvider.findByModel(resolvedModel)
			.orElseThrow(() -> AiProviderNotFoundException.from(resolvedModel));

		return new SpringAiProviderModel(aiProvider, resolvedModel);
	}

	private static Model<?, ?> resolveModel(Model<?, ?> model) {

		return model instanceof ChatModelWrapper chatModelWrapper
			? chatModelWrapper.getChatModel()
			: model;
	}

	public SpringAiProviderModel {
		Assert.notNull(aiProvider, "AiProvider is required");
		Assert.notNull(model, "AiProvider [%s] Model is required", aiProvider.getName());
	}

	private String getModelName(Model<?, ?> model) {
		return ModelNameResolver.defaultModelNameResolver().resolveName(model);
	}

	@Override
	public String getName() {
		return aiProvider().getName();
	}

	@SuppressWarnings("unchecked")
	public <T extends Model<?, ?>> T getTypedModel() {
		return (T) model();
	}
}
