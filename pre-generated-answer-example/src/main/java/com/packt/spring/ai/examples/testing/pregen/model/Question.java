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

import org.springframework.ai.document.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Abstract Data Type (ADT) modeling a user question (prompt).
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Question(Document document) {

	public static Question empty() {
		return from("?");
	}

	public static Question from(String question) {
		Assert.hasText(question, "Question is required");
		return new Question(buildDocument(question));
	}

	private static Document buildDocument(String content) {

		return Document.builder()
			.withId(UUID.randomUUID().toString())
			.withContent(content)
			.build();
	}

	public Question {
		Assert.notNull(document, "Document is required");
	}

	public String get() {
		return document().getContent();
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
}
