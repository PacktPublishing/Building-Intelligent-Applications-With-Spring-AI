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

import org.springframework.ai.document.Document;

/**
 * Service interface used to estimate the number of tokens in given block of {@link String content},
 * such as a body of {@link String plain text}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface TokenCountService {

	/**
	 * Counts the number of tokens in the given {@link String content}.
	 *
	 * @param content {@link String} containing the content used to count tokens.
	 * @return the number of token estimated from the given {@link String content}.
	 * @see #countFilteredTokens(String)
	 */
	int countTokens(String content);

	/**
	 * Counts the number of tokens in the given {@link Document}.
	 *
	 * @param document {@link Document} containing the {@link String content} used to count tokens.
	 * @return the number of token estimated from the given {@link Document}.
	 * @see org.springframework.ai.document.Document
	 * @see #countTokens(String)
	 */
	default int countTokens(Document document) {
		return document != null ? countTokens(document.getContent()) : 0;
	}

	/**
	 * Counts the number of tokens in the given {@link String content} after having been filtered
	 * for non-essential words and punctuation.
	 * <p>
	 * By default, there is no difference between token count and filtered token count.
	 *
	 * @param content {@link String} containing the content used to count tokens.
	 * @return the number of token estimated from the given, filtered {@link String content}.
	 * @see #countTokens(String)
	 */
	default int countFilteredTokens(String content) {
		return countTokens(content);
	}

	/**
	 * Counts the number of tokens in the given {@link Document} after having been filtered
	 * for non-essential words and punctuation.
	 *
	 * @param document {@link Document} containing the content used to count tokens.
	 * @return the number of token estimated from the given, filtered {@link Document}.
	 * @see #countFilteredTokens(String)
	 */
	default int countFilteredTokens(Document document) {
		return document != null ? countFilteredTokens(document.getContent()) : 0;
	}
}
