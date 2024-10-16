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
package com.packt.spring.ai.examples.testing.pregen.model;

import java.util.Iterator;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.packt.spring.ai.examples.testing.pregen.util.Utils;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling a how-to {@link Question} with an associated {@link Answer}.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see com.packt.spring.ai.examples.testing.pregen.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregen.model.Nameable
 * @see com.packt.spring.ai.examples.testing.pregen.model.Question
 * @see com.packt.spring.ai.examples.testing.pregen.model.Questions
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class HowTo implements Iterable<Question>, Nameable<String> {

	public static HowTo from(Question question, Answer answer) {
		Questions questions = Questions.of(assertQuestion(question));
		return new HowTo(questions, answer);
	}

	private static Answer assertAnswer(Answer answer) {
		Assert.notNull(answer, "Answer is required");
		return answer;
	}

	private static Question assertQuestion(Question question) {
		Assert.notNull(question, "Question is required");
		return question;
	}

	private final Answer answer;

	private volatile Questions questions;

	private String name;

	@JsonCreator
	protected HowTo(@JsonProperty("questions") Questions questions, @JsonProperty("answer") Answer answer) {
		this.questions = Questions.nullSafe(questions);
		this.answer = assertAnswer(answer);
	}

	public synchronized HowTo add(Question question) {
		this.questions = getQuestions().add(question);
		return this;
	}

	public Question findFirstQuestion() {
		return stream().findFirst().orElseGet(Question::empty);
	}

	public synchronized boolean has(Question question) {
		return getQuestions().contains(question);
	}

	@Override
	public @NonNull Iterator<Question> iterator() {
		return getQuestions().iterator();
	}

	public HowTo named(String name) {
		this.name = name;
		return this;
	}

	public Stream<Question> stream() {
		return Utils.stream(this);
	}
}
