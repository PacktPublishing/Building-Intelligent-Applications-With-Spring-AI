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

import java.util.UUID;

import com.packt.spring.ai.examples.testing.pregen.util.Utils;

import org.springframework.ai.document.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) modeling a user question (prompt).
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregen.model.Answer
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Question(String name, Document document, Answer answer) implements Nameable<String> {

	private static final Answer NO_ANSWER = null;

	public Question {
		name = resolveName(name);
		Utils.assertDocument(document);
	}

	public static Question.Builder builder(String question) {
		return new Question.Builder(question);
	}

	public static Question.Builder copy(Question question) {

		Assert.notNull(question, "Question to copy is required");

		return new Question.Builder(question.get())
			.answered(question.answer())
			.named(question.getName());
	}

	public static Question empty() {
		return from("?");
	}

	public static Question from(String question) {
		return new Question(randomName(), buildDocument(question), NO_ANSWER);
	}

	private static Document buildDocument(String content) {

		return Document.builder()
			.withId(UUID.randomUUID().toString())
			.withContent(Utils.assertQuestion(content))
			.build();
	}

	private static String randomName() {
		return UUID.randomUUID().toString();
	}

	private static String resolveName(String name) {
		return StringUtils.hasText(name) ? name : randomName();
	}

	public String get() {
		return document().getContent();
	}

	@Override
	public String getName() {
		return name();
	}

	public boolean hasAnswer() {
		return Answer.isNotUnknown(answer());
	}

	public boolean isMatch(Document document) {
		return document != null && isMatch(document.getContent());
	}

	public boolean isMatch(Question question) {
		return question != null && isMatch(question.get());
	}

	public boolean isMatch(String question) {
		return StringUtils.hasText(question) && document().getContent().equalsIgnoreCase(question);
	}

	@Override
	public String toString() {
		return get();
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Builder {

		private Answer answer;

		private String name;

		private final String question;

		protected Document getDocument() {
			return buildDocument(getQuestion());
		}

		public Builder answered(Answer answer) {
			this.answer = answer;
			return this;
		}

		public Builder named(String name) {
			this.name = name;
			return this;
		}

		public Question build() {
			return new Question(getName(), getDocument(), getAnswer());
		}
	}
}
