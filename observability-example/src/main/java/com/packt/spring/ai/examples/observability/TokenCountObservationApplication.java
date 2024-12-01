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
package com.packt.spring.ai.examples.observability;

import com.knuddels.jtokkit.api.EncodingType;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to demonstrate Observability with Micrometer.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see io.micrometer.core.instrument.Counter
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.tokenizer.TokenCountEstimator
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 */
@SpringBootApplication
@Profile(TokenCountObservationApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class TokenCountObservationApplication extends AbstractSpringBootApplication {

	protected static final String SPRING_APPLICATION_PROFILE = "token-count-observation";
	protected static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(TokenCountObservationApplication.class)
			.profiles(SPRING_APPLICATION_PROFILE, USER_PROFILE)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}

	@Bean
	SimpleMeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

	@Bean
	TokenCountEstimator tokenCountEstimator() {
		return new JTokkitTokenCountEstimator(EncodingType.O200K_BASE);
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient, MeterRegistry meterRegistry,
			TokenCountEstimator tokenCountEstimator) {

		return readEvaluatePrintLoop((args, input) -> {

			Prompt prompt = new Prompt(input);

			ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

			String generatedContent = outputAiResponse(getContent(chatResponse));

			// Metadata & Metrics
			print("%nAI Provider Token Count [%s]%n", getAiProviderTokenCount(chatResponse));
			print("Estimated Token Count [%s]%n", getEstimatedTokenCount(prompt, generatedContent, tokenCountEstimator));
			print("Observed Token Count [%s]%n", getObservedTokenCount(meterRegistry));
		});
	}

	private Long getAiProviderTokenCount(ChatResponse chatResponse) {

		ChatResponseMetadata metadata = chatResponse.getMetadata();

		return metadata.getUsage().getTotalTokens();
	}

	@SuppressWarnings("all")
	private int getEstimatedTokenCount(Prompt prompt, String generatedContent, TokenCountEstimator tokenCountEstimator) {

		String promptContent = prompt.getContents();

		int promptTokenCount = tokenCountEstimator.estimate(promptContent);
		int generatedContentTokenCount = tokenCountEstimator.estimate(generatedContent);
		int totalTokenCount = promptTokenCount + generatedContentTokenCount;

		return totalTokenCount;
	}

	private Double getObservedTokenCount(MeterRegistry meterRegistry) {

		return meterRegistry.getMeters().stream()
			.filter(meter -> "gen_ai.client.token.usage".equalsIgnoreCase(meter.getId().getName()))
			.findFirst()
			.filter(Counter.class::isInstance)
			.map(Counter.class::cast)
			.map(Counter::count)
			.orElse(0.0d);
	}
}
