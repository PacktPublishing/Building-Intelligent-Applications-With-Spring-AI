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

import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.model.NamedModel;
import io.codeprimate.extensions.spring.ai.provider.model.NamedModels;

/**
 * {@link AiProvider} implementation for {@literal OpenAI}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see io.codeprimate.extensions.spring.ai.provider.model.NamedModels
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class OpenAiProvider implements AiProvider {

	public static final OpenAiProvider INSTANCE = new OpenAiProvider();

	private static final String NAME = "OpenAI";

	private static final NamedModels NAMED_MODELS = NamedModels.of(
		NamedModel.builder("gpt-5o").asChat().asImage().asText().withContextWindow(400_000).build(),
		NamedModel.builder("gpt-5o-mini").asChat().asImage().asText().withContextWindow(400_000).build(),
		NamedModel.builder("gpt-5o-nano").asChat().asImage().asText().withContextWindow(400_000).build(),
		NamedModel.builder("gpt-4.1").asChat().asImage().asText().withContextWindow(1_047_576).build(),
		NamedModel.builder("gpt-4.1-mini").asChat().asImage().asText().withContextWindow(1_047_576).build(),
		NamedModel.builder("gpt-4.1-nano").asChat().asImage().asText().withContextWindow(1_047_576).build(),
		NamedModel.builder("gpt-4o").asChat().asImage().asText().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4o-mini").asChat().asImage().asText().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4o-audio-preview").asAudio().asText().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4o-mini-audio-preview").asAudio().asText().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4o-realtime-preview").asAudio().asText().withContextWindow(32_000).build(),
		NamedModel.builder("gpt-4o-mini-realtime-preview").asAudio().asText().withContextWindow(16_000).build(),
		NamedModel.builder("gpt-4o-search-preview").asText().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4o-mini-search-preview").asText().withContextWindow(128_000).build(),

		NamedModel.builder("o1").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o1-mini").asText().withContextWindow(128_000).build(),
		NamedModel.builder("o1-pro").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o3").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o3-mini").asText().withContextWindow(200_000).build(),
		NamedModel.builder("o3-pro").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o3-deep-research").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o4-mini").asImage().asText().withContextWindow(200_000).build(),
		NamedModel.builder("o4-mini-deep-research").asImage().asText().withContextWindow(200_000).build(),

		NamedModel.builder("gpt-4").asChat().asImage().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4-turbo").asChat().asImage().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-4-turbo-preview").asChat().asImage().withContextWindow(128_000).build(),
		NamedModel.builder("gpt-3.5-turbo").asChat().withContextWindow(16_385).build(),
		NamedModel.builder("gpt-3.5-turbo-instruct").asChat().withContextWindow(4096).build(),
		NamedModel.builder("dall-e-2").asImage().build(),
		NamedModel.builder("dall-e-3").asImage().build(),
		NamedModel.builder("tts-1").asTextToSpeech().build(),
		NamedModel.builder("tts-1-hd").asTextToSpeech().build(),
		NamedModel.builder("whisper-1").asTextToSpeech().build(),
		NamedModel.builder("text-embedding-3-large").asEmbedding().withDimensions(3072).build(),
		NamedModel.builder("text-embedding-3-small").asEmbedding().withDimensions(1536).build(),
		NamedModel.builder("text-embedding-ada-002").asEmbedding().withDimensions(1536).build(),
		NamedModel.builder("omni-moderation-latest").asModeration().withContextWindow(32_768).build(),
		NamedModel.builder("text-moderation-latest").asModeration().withContextWindow(32_768).build(),
		NamedModel.builder("text-moderation-stable").asModeration().withContextWindow(32_768).build(),
		NamedModel.builder("text-moderation-007").asModeration().withContextWindow(32_768).build(),
		NamedModel.builder("babbage-002").asFoundation().withContextWindow(16_384).build(),
		NamedModel.builder("davinci-002").asFoundation().withContextWindow(16_384).build()
	);

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Iterable<NamedModel> namedModels() {
		return NAMED_MODELS;
	}
}
