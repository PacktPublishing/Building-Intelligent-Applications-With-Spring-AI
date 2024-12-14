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

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.StringUtils;
import org.springframework.ai.model.Model;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of well-known, {@link org.cp.elements.lang.Nameable named} {@link AiProvider AI providers}
 * supported by Spring AI.
 *
 * @author John Blum
 * @see java.lang.Enum
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see org.springframework.ai.model.Model
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public enum SpringAiProvider implements AiProvider {

	ANTHROPIC("Anthropic", match("anthropic").or(match("claude"))),
	AZURE_OPEN_AI("Microsoft Azure OpenAI", match("azure").and(match("openai"))),
	BEDROCK_ANTHROPIC3("Amazon Bedrock Anthropic3", match("bedrock").and(match("anthropic3"))),
	BEDROCK_ANTHROPIC("Amazon Bedrock Anthropic", match("bedrock").and(match("anthropic"))),
	BEDROCK_COHERE("Amazon Bedrock Cohere", match("bedrock").and(match("cohere"))),
	BEDROCK_CONVERSE("Amazon Bedrock Converse", match("bedrock").and(match("converse").or(match("proxy")))),
	BEDROCK_JURASSIC2("Amazon Bedrock AI21 Jurassic2", match("bedrock").and(match("jurassic2"))),
	BEDROCK_LLAMA("Amazon Bedrock Llama", match("bedrock").and(match("llama"))),
	BEDROCK_TITAN("Amazon Bedrock Titan", match("bedrock").and(match("titan"))),
	HUGGING_FACE("Hugging Face", match("huggingface")),
	MINI_MAX("MiniMax AI", match("minimax")),
	MISTRAL_AI("Mistral AI", match("mistral")),
	MOCK("Mock AI", match("mock")),
	MOONSHOT("Moonshot AI", match("moonshot")),
	OCI_COHERE("Oracle OCI Cohere AI", match("oci").and(match("cohere"))),
	OCI("Oracle OCI AI", match("oci")),
	OLLAMA("Ollama", match("ollama")),
	OPEN_AI("OpenAI", match("openai")),
	POSTGRES_ML("Postgres ML", match("postgres")),
	QIAN_FAN("Qian Fan AI", match("qianfan")),
	STABILITY("Stability AI", match("stability")),
	VERTEX_AI_GEMINI("Google Vertex AI Gemini", match("gemini")),
	WATSONX("IBM Watsonx AI", match("watson")),
	ZHI_PU("Zhipu AI", match("zhipu"));

	private final String name;
	private final Predicate<Object> predicate;

	SpringAiProvider(String name, Predicate<Object> predicate) {
		this.name = StringUtils.requireText(name, "Name of AI provider is required");
		this.predicate = ObjectUtils.requireObject(predicate, "Predicate used to match an enum is required");
	}

	public static Optional<? extends AiProvider> findBy(Predicate<AiProvider> predicate) {
		return stream().filter(predicate).findFirst();
	}

	public static Optional<? extends AiProvider> findByModel(Model<?, ?> model) {

		return findBy(aiProvider -> resolveAiProvider(aiProvider) instanceof SpringAiProvider springAiProvider
			&& springAiProvider.getPredicate().test(model));
	}

	public static Optional<? extends AiProvider> findByName(String name) {
		return findBy(aiProvider -> StringUtils.hasText(name)
			&& aiProvider.getName().toLowerCase().contains(name.toLowerCase()));
	}

	private static Predicate<Object> match(String qualifier) {
		String resolveQualifier = String.valueOf(qualifier).toLowerCase();
		return target -> Utils.nullSafeTypeName(target).toLowerCase().contains(resolveQualifier);
	}

	private static AiProvider resolveAiProvider(AiProvider aiProvider) {

		return aiProvider instanceof SpringAiProviderModel aiProviderModel
			? aiProviderModel.aiProvider()
			: aiProvider;
	}

	public static Stream<SpringAiProvider> stream() {
		return Arrays.stream(values());
	}
}
