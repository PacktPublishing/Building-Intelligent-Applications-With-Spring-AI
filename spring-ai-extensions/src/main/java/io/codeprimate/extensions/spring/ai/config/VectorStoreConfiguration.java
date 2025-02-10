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
package io.codeprimate.extensions.spring.ai.config;

import io.codeprimate.extensions.spring.ai.vectorstore.DecoratedSimpleVectorStore;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration} used to configure and initialize a Spring AI {@link VectorStore}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.vectorstore.DecoratedSimpleVectorStore
 * @see org.springframework.ai.embedding.EmbeddingModel
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 */
@Configuration
@SuppressWarnings("unused")
public class VectorStoreConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public VectorStore inMemoryVectorStore(EmbeddingModel embeddingModel,
			@Autowired(required = false) ObservationRegistry observationRegistry,
			@Autowired(required = false) VectorStoreObservationConvention observationConvention) {

		SimpleVectorStore.SimpleVectorStoreBuilder vectorStoreBuilder =
			simpleVectorStoreBuilder(embeddingModel, observationRegistry, observationConvention);

		return new DecoratedSimpleVectorStore(vectorStoreBuilder);
	}

	protected SimpleVectorStore.SimpleVectorStoreBuilder simpleVectorStoreBuilder(EmbeddingModel embeddingModel,
			ObservationRegistry observationRegistry, VectorStoreObservationConvention observationConvention) {

		SimpleVectorStore.SimpleVectorStoreBuilder vectorStoreBuilder = SimpleVectorStore.builder(embeddingModel);

		vectorStoreBuilder = observationRegistry != null
			? vectorStoreBuilder.observationRegistry(observationRegistry)
			: vectorStoreBuilder;

		vectorStoreBuilder = observationConvention != null
			? vectorStoreBuilder.customObservationConvention(observationConvention)
			: vectorStoreBuilder;

		return vectorStoreBuilder;
	}
}
