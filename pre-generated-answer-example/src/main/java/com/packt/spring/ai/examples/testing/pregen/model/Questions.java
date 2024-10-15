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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.packt.spring.ai.examples.testing.pregen.util.Utils;

import org.springframework.ai.document.Document;

/**
 * {@link Iterable Collecction} of {@link Question Questions}.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see com.packt.spring.ai.examples.testing.pregen.model.Question
 * @since 1.0.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface Questions extends Iterable<Question> {

	static Questions empty() {
		return of();
	}

	static Questions nullSafe(Questions questions) {
		return questions != null ? questions : empty();
	}

	static Questions of(Question... questions) {
		return of(Arrays.asList(questions));
	}

	@JsonCreator
	static Questions of(Iterable<Question> questions) {
		return questions::iterator;
	}

	default Questions add(Question question) {

		if (question != null) {
			Set<Question> questions = new HashSet<>(toSet());
			questions.add(question);
			return of(questions);
		}

		return this;
	}

	default boolean contains(Question question) {
		return question != null && findBy(question::isMatch).isPresent();
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	default Optional<Question> findBy(Predicate<Question> predicate) {
		return stream().filter(predicate).findFirst();
	}

	default Optional<Question> findBy(Document document) {
		return findBy(question -> question.isMatch(document));
	}

	default int size() {
		return Long.valueOf(stream().count()).intValue();
	}

	default Stream<Question> stream() {
		return Utils.stream(this);
	}

	default List<Question> toList() {
		return stream().toList();
	}

	default Set<Question> toSet() {
		return stream().collect(Collectors.toSet());
	}
}
