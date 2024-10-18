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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.util;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;

import org.springframework.ai.document.Document;
import org.springframework.util.Assert;

/**
 * Abstract utility class for assertions used by the Questions and Answers application.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils
 */
@SuppressWarnings("unused")
public abstract class Assertions extends Utils {

	public static Answer assertAnswer(Answer answer) {
		Assert.notNull(answer, "Answer is required");
		return answer;
	}

	public static void assertDocument(Document document) {
		Assert.notNull(document, "Document is required");
		assertQuestion(document.getContent());
	}

	public static void assertEmbedding(Question question) {
		Assert.state(isEmbeddingPresent(question), () -> "Expected Embedding for Question [%s]".formatted(question));
	}

	public static HowTo assertHowTo(HowTo howTo) {
		Assert.notNull(howTo, "HowTo is required");
		return howTo;
	}

	public static Question assertQuestion(Question question) {
		Assert.notNull(question, "Question is required");
		return question;
	}

	public static String assertQuestion(String question) {
		Assert.hasText(question, () -> "Question [%s] is required".formatted(question));
		return question;
	}
}
