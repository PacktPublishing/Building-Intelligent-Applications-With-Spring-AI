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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.repo;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Nameable;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.ai.document.Document;

/**
 *  Data Access Object (DAO) used to perform basic CRUD and simple query data access operations
 *  on {@link HowTo} objects.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.lang.Iterable
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface HowToRepository extends Iterable<HowTo> {

	default long count() {
		return stream().count();
	}

	default Optional<HowTo> findBy(Document... documents) {
		return findBy(Arrays.asList(documents));
	}

	default Optional<HowTo> findBy(Iterable<Document> documents) {

		return stream()
			.filter(howTo -> Utils.stream(documents).anyMatch(document ->
				howTo.getQuestions().findBy(document).isPresent()))
			.findFirst();
	}

	default Optional<HowTo> findBy(Question question) {

		return stream()
			.filter(howTo -> howTo.has(question))
			.findFirst();
	}

	@SuppressWarnings("unchecked")
	default boolean load(Nameable<String>... namedHowTos) {
		return false;
	}

	default boolean save(HowTo howTo) {
		throw new UnsupportedOperationException("Save Not Implemented");
	}

	default Stream<HowTo> stream() {
		return Utils.stream(this);
	}
}
