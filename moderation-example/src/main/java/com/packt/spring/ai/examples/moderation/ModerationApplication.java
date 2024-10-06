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
package com.packt.spring.ai.examples.moderation;

import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootApplication} using Spring AI with OpenAI and ChatGPT ({@literal gpt-4o} model) to demonstrate
 * the Moderation API.
 *
 * @author John Blum
 * @see org.springframework.ai.moderation.Moderation
 * @see org.springframework.ai.moderation.ModerationPrompt
 * @see org.springframework.ai.moderation.ModerationResponse
 * @see org.springframework.ai.openai.OpenAiModerationModel
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ModerationApplication {

	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(ModerationApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	private void print(String label, Object... arguments) {
		System.out.printf(label, arguments);
		System.out.flush();
	}

	@Bean
	ApplicationRunner programRunner(OpenAiModerationModel moderationModel) {

		return args -> {

			ModerationPrompt prompt = new ModerationPrompt("Strike First! Strike Hard! No Mercy!");

			print("prompt> %s%n%n", prompt.getInstructions().getText());

			ModerationResponse response = moderationModel.call(prompt);

			Moderation moderation = response.getResult().getOutput();

			for (ModerationResult moderationResult : moderation.getResults()) {

				print("Flagged: %s%n", moderationResult.isFlagged());

				Categories categories = moderationResult.getCategories();

				print("Harassment: %s%n", categories.isHarassment());
				print("Hate: %s%n", categories.isHate());
				print("Self-Harm: %s%n", categories.isSelfHarm());
				print("Self-Harm Instructions: %s%n", categories.isSelfHarmInstructions());
				print("Self-Harm Intent: %s%n", categories.isSelfHarmIntent());
				print("Sexual: %s%n", categories.isSexual());
				print("Violence: %s%n", categories.isViolence());

				CategoryScores scores = moderationResult.getCategoryScores();

				print("Harassment Score: %s%n", scores.getHarassment());
				print("Hate Score: %s%n", scores.getHate());
				print("Self-Harm Score: %s%n", scores.getSelfHarm());
				print("Self-Harm Instructions Score: %s%n", scores.getSelfHarmInstructions());
				print("Self-Harm Intent Score: %s%n", scores.getSelfHarmIntent());
				print("Sexual Score: %s%n", scores.getSexual());
				print("Violence Score: %s%n", scores.getViolence());
			}
		};
	}
}
