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
package io.codeprimate.tools.spring.ai.tokens.service;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.document.Document;

/**
 * Service interface to generate tokens from {@link String content} containing words.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface TokenizerService {

	String DEFAULT_MODEL_NAME = "gpt-4o-mini";

	/**
	 * Tokenizes the given {@link String content} based on OpenAI's {@literal gpt-4o-mini} model.
	 *
	 * @param content {@link String} containing the content to tokenize.
	 * @return a {@link List} of {@link String tokens} generated from the given {@link String content}.
	 * @see #tokenize(String, String)
	 */
	default List<String> tokenize(String content) {
		return tokenize(content, DEFAULT_MODEL_NAME);
	}

	/**
	 * Tokenizes the given {@link String content} based on the given {@link String named} AI model
	 *
	 * @param content {@link String} containing the content to tokenize.
	 * @param modelName {@link String} containing the name of the AI model.
	 * @return a {@link List} of {@link String tokens} generated from the given {@link String content}.
	 */
	List<String> tokenize(String content, String modelName);

	/**
	 * Tokenizes the {@link String content} from the given {@link Document}
	 * based on OpenAI's {@literal gpt-4o-mini} model.
	 *
	 * @param document {@link Document} containing the {@link String content} to tokenize.
	 * @return a {@link List} of {@link String tokens} generated from the given {@link Document}.
	 * @see org.springframework.ai.document.Document
	 * @see #tokenize(String, String)
	 */
	default List<String> tokenize(Document document) {
		return tokenize(document, DEFAULT_MODEL_NAME);
	}

	/**
	 * Tokenizes the {@link String content} from the given {@link Document}
	 * based on the given {@link String named} AI model
	 *
	 * @param document {@link Document} containing the {@link String content} to tokenize.
	 * @param modelName {@link String} containing the name of the AI model.
	 * @return a {@link List} of {@link String tokens} generated from the given {@link Document}.
	 * @see org.springframework.ai.document.Document
	 * @see #tokenize(String, String)
	 */
	default List<String> tokenize(Document document, String modelName) {
		return document != null ? tokenize(document.getContent(), modelName) : Collections.emptyList();
	}
}
