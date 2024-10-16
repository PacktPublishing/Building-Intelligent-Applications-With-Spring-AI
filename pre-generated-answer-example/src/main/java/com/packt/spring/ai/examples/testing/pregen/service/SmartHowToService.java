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
package com.packt.spring.ai.examples.testing.pregen.service;

import java.util.List;

import com.packt.spring.ai.examples.testing.pregen.model.Answer;
import com.packt.spring.ai.examples.testing.pregen.model.HowTo;
import com.packt.spring.ai.examples.testing.pregen.model.Question;
import com.packt.spring.ai.examples.testing.pregen.repo.HowToRepository;
import com.packt.spring.ai.examples.testing.pregen.util.AnswerNotFoundException;
import com.packt.spring.ai.examples.testing.pregen.util.Utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} used to answer how-to questions.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregen.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregen.model.HowTo
 * @see com.packt.spring.ai.examples.testing.pregen.model.Question
 * @see com.packt.spring.ai.examples.testing.pregen.repo.HowToRepository
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("all")
public class SmartHowToService implements HowToService {

	@Value("${example.app.pre-generated-answers.embeddings.similarity-threshold:0.75}")
	private double simililarityThreshold;

	@Value("${example.app.pre-generated-answers.embeddings.top-k:10}")
	private int topK;

	private final HowToRepository repository;

	private final VectorStore vectorStore;

	@Override
	public Answer answer(Question question) {
		throw new UnsupportedOperationException("Generated Answers Not Supported");
	}

	@Override
	public Answer howTo(Question question) {

		Assert.notNull(question, "Question is required");

		return getRepository().findBy(question)
			.map(HowTo::getAnswer)
			.orElseGet(() -> findAnswerBySimilarQuestions(question));
	}

	protected Answer findAnswerBySimilarQuestions(Question question) {

		List<Document> similarDocuments = findSimilarDocuments(question);

		return Utils.isNotEmpty(similarDocuments)
			? findAnswerBySimilarDocuments(question, similarDocuments)
			: answer(question);
	}

	protected List<Document> findSimilarDocuments(Question question) {

		String query = question.get();

		SearchRequest request = SearchRequest.query(query)
			.withSimilarityThreshold(getSimililarityThreshold())
			.withTopK(getTopK());

		return getVectorStore().similaritySearch(request);
	}

	protected Answer findAnswerBySimilarDocuments(Question question, List<Document> similarDocuments) {

		return getRepository().findBy(similarDocuments)
			.map(howTo -> howTo.add(store(question)))
			.map(HowTo::getAnswer)
			.orElseThrow(() -> AnswerNotFoundException.from(question));
	}

	protected HowTo save(HowTo howTo) {
		getRepository().save(howTo);
		return howTo;
	}

	protected Question store(Question question) {
		getVectorStore().accept(List.of(question.document()));
		return question;
	}
}
