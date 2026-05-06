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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Nameable;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link Repository} and {@link InMemoryHowToRepository} implementation used to persist {@link HowTo} objects
 * as JSON to the filesystem.
 *
 * @author John Blum
 * @see java.io.File
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Nameable
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.InMemoryHowToRepository
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.io.Resource
 * @see org.springframework.stereotype.Repository
 * @see tools.jackson.databind.ObjectMapper
 * @since 0.1.0
 */
@Primary
@Repository
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Profile("pre-generated-answers")
public class JsonFileHowToRepository extends InMemoryHowToRepository {

	private static final String JSON_FILE_EXTENSION = ".json";

	private final Environment environment;

	private final ObjectMapper objectMapper;

	private final VectorStore vectorStore;

	@Override
	@SuppressWarnings("unchecked")
	public boolean load(Nameable<String>... names) {

		return Arrays.stream(names)
			.map(namedHowTo -> {
				try {
					Resource jsonFile = resolveResource(namedHowTo);
					HowTo howTo = loadJson(jsonFile, HowTo.class);
					return super.save(store(howTo));
				}
				catch (IOException e) {
					return false;
				}
			})
			.reduce((one, two) -> one && two)
			.orElse(false);
	}

	@Override
	public boolean save(HowTo howTo) {

		Assert.state(super.save(howTo), () -> "Failed to save HowTo [%s] in memory".formatted(howTo));

		return isPersistenceNotEnabled() || doSave(howTo);
	}

	private boolean doSave(HowTo howTo) {
		File jsonFile = toJsonFile(howTo);
		getObjectMapper().writeValue(jsonFile, howTo);
		return true;
	}

	private boolean isPersistenceEnabled() {
		return getEnvironment().matchesProfiles("ai-enabled-answers");
	}

	private boolean isPersistenceNotEnabled() {
		return !isPersistenceEnabled();
	}

	@SuppressWarnings("all")
	private <T> T loadJson(Resource jsonFile, Class<T> type) throws IOException {
		return getObjectMapper().readValue(jsonFile.getContentAsByteArray(), type);
	}

	private ClassPathResource resolveResource(Nameable<String> namedObject) {
		return new ClassPathResource(toJsonFile(namedObject).getName());
	}

	private <T extends Iterable<Question>> T store(T questions) {
		List<Document> documents = Utils.stream(questions).map(Question::document).toList();
		getVectorStore().accept(documents);
		return questions;
	}

	private File toJsonFile(Nameable<String> namedObject) {
		return new File(System.getProperty("user.dir"), namedObject.getName().concat(JSON_FILE_EXTENSION));
	}
}
