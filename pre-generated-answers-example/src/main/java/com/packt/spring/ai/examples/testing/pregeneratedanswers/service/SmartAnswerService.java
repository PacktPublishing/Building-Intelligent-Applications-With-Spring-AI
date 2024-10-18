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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.service;

import java.util.List;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.AnswerNotFoundException;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Assertions;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} used to {@link Answer answer} {@link Question questions}.
 * <p/>
 * {@link Answer Answers} mainly focus on {@literal how-to} type {@link Question questions}.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("all")
public class SmartAnswerService implements AnswerService {

	@Value("${example.app.pre-generated-answers.embeddings.similarity-threshold:0.75}")
	private double similarityThreshold;

	@Value("${example.app.pre-generated-answers.embeddings.top-k:10}")
	private int topK;

	private final HowToRepository repository;

	private final VectorStore vectorStore;

	@Override
	public Answer answer(Question question) {

		Assertions.assertQuestion(question);

		return getRepository().findBy(question)
			.map(HowTo::getAnswer)
			.orElseGet(() -> findAnswerFromSimilarQuestions(question));
	}

	protected Answer findAnswerFromSimilarQuestions(Question question) {

		List<Document> similarDocuments = findSimilarDocuments(question);

		if (Utils.isNotEmpty(similarDocuments)) {

			Answer answer = findAnswerBySimilarDocuments(question, similarDocuments);

			if (Answer.isNotUnknown(answer)) {
				return answer;
			}
		}

		throw AnswerNotFoundException.from(question);
	}

	protected List<Document> findSimilarDocuments(Question question) {

		String query = question.get();

		SearchRequest request = SearchRequest.query(query)
			.withSimilarityThreshold(getSimilarityThreshold())
			.withTopK(getTopK());

		return getVectorStore().similaritySearch(request);
	}

	protected Answer findAnswerBySimilarDocuments(Question question, List<Document> similarDocuments) {

		return getRepository().findBy(similarDocuments)
			.map(howTo -> howTo.add(store(question)))
			.map(HowTo::getAnswer)
			.orElse(Answer.UNKNOWN);
	}

	protected HowTo save(HowTo howTo) {
		getRepository().save(howTo);
		return howTo;
	}

	protected Question store(Question question) {
		List<Document> documents = List.of(question.document());
		getVectorStore().accept(documents);
		return question;
	}
}
