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
package com.packt.spring.ai.examples.testing.pregeneratedanswers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Questions;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.service.AnswerService;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.AnswerNotFoundException;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests for {@link PreGeneratedAnswersApplication}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("pre-generated-answers")
@SuppressWarnings("unused")
public class PreGeneratedAnswersIntegrationTests {

	@Autowired
	private AnswerService answerService;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Autowired
	private HowToRepository repository;

	@BeforeEach
	public void assertTwoAnswers() {
		assertThat(this.repository.count()).isEqualTo(2L);
	}

	@Test
	void howToSolveLinearEquationReturnsAnswer() {

		Answer answer = this.answerService.answer(Question.from("How to solve a linear equation?"));

		assertThat(answer).isNotNull();
	}

	@Test
	void howToSolveQuadraticEquationWithSimilarQuestionReturnsAnswer() {

		Questions questions = Questions.of(
			Question.from("How do I solve a quadratic equation?"),
			Question.from("I am trying to solve a quadratic equation.")
		);

		//logSimilarities(questions);

		List<Question> questionList = questions.toList();

		Answer answer = this.answerService.answer(questionList.get(0));

		assertThat(answer).isNotNull();

		Answer answerAgain = this.answerService.answer(questionList.get(1));

		assertThat(answerAgain).isSameAs(answer);
	}

	@Test
	void questionWithNoAnswer() {

		assertThatExceptionOfType(AnswerNotFoundException.class)
			.isThrownBy(() -> this.answerService.answer(Question.from("How do I tie my shoes?")))
			.withMessage("Answer to Question [How do I tie my shoes?] not found")
			.withNoCause();
	}

	private void logSimilarities(Questions questions) {

		questions.forEach(question -> {
			float[] questionEmbedding = this.embeddingModel.embed(question.get());
			Utils.print("Question [%s] is similar to:%n", question);
			this.repository.stream().flatMap(HowTo::stream).forEach(howToQuestion -> {
				float[] howToQuestionEmbedding = howToQuestion.document().getEmbedding();
				double similarity = SimpleVectorStore.EmbeddingMath.cosineSimilarity(questionEmbedding, howToQuestionEmbedding);
				Utils.print(">>> [%s] = %s%n", howToQuestion, similarity);
			});
		});
	}
}
