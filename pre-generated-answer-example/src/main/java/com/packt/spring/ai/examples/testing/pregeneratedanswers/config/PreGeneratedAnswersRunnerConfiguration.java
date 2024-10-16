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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.config;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Nameable;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * {@link SpringBootConfiguration} or all {@link ApplicationRunner ApplicationRunners}.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringBootConfiguration
 * @since 0.1.0
 */
@SpringBootConfiguration
@SuppressWarnings("unused")
public class PreGeneratedAnswersRunnerConfiguration {

	protected static final Set<Question> NAMED_QUESTIONS = Set.of(
		Question.builder("How to solve a linear equation?").named("howToSolveLinearEquations").build(),
		Question.builder("How to solve a quadratic equation?").named("howToSolveQuadraticEquations").build()
	);

	@Bean
	@Profile("!pre-generated-answers")
	@SuppressWarnings({ "unchecked" })
	ApplicationRunner loadPreGeneratedAnswersRunner(HowToRepository repository) {

		return args -> {

			Utils.print("Loading Pre-Generated Answers...%n");

			repository.load(NAMED_QUESTIONS.toArray(new Nameable[0]));
		};
	}

	@Bean
	@Profile("pre-generated-answers")
	ApplicationRunner preGenerateAnswersRunner(ChatClient chatClient, EmbeddingModel embeddingModel,
			HowToRepository repository) {

		return args -> NAMED_QUESTIONS
			.forEach(question -> {

				Utils.print("Creating Pre-Generated Answers...%n");

				String stringQuestion = question.get();

				Prompt prompt = new Prompt(stringQuestion);

				String stringAnswer = chatClient.prompt(prompt).call().content();

				Answer answer = Answer.from(stringAnswer);

				question.document().setEmbedding(embeddingModel.embed(stringQuestion));

				HowTo howTo = HowTo.from(question, answer).named(question.getName());

				Assert.state(repository.save(howTo), () -> "Failed to save HowTo [%s]".formatted(howTo));
			});
	}

	@Bean
	@Profile("json-serialization-test")
	ApplicationRunner howToJsonDeserializationSerializationRunner(ObjectMapper objectMapper) {

		return args -> {

			Resource json = new ClassPathResource("howToSolveLinearEquations.json");
			HowTo howTo = objectMapper.readValue(json.getContentAsByteArray(), HowTo.class);

			Utils.print("JSON [%s]%n",
				objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(howTo));
		};
	}
}
