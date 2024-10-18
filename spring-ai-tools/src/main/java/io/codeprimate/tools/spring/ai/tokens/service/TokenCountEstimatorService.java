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
import org.springframework.util.StringUtils;

/**
 * Service interface used to estimate the token and word count for a given block of {@link String text content}.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface TokenCountEstimatorService {

	/**
	 * Counts the number of tokens in the given {@link String content}.
	 *
	 * @param content {@link String} containing the content used to count tokens.
	 * @return the number of token estimated from the given {@link String content}.
	 * @see #filteredTokenCount(String)
	 */
	int tokenCount(String content);

	/**
	 * Counts the number of tokens in the given {@link Document}.
	 *
	 * @param document {@link Document} containing the content used to count tokens.
	 * @return the number of token estimated from the given {@link Document}.
	 * @see org.springframework.ai.document.Document
	 * @see #tokenCount(Document)
	 */
	default int tokenCount(Document document) {
		return document != null ? tokenCount(document.getContent()) : 0;
	}

	/**
	 * Counts the number of tokens in the given {@link String content} after having been filtered
	 * for non-essential words.
	 *
	 * @param content {@link String} containing the content used to count tokens.
	 * @return the number of token estimated from the given, filtered {@link String content}.
	 * @see #tokenCount(String)
	 */
	int filteredTokenCount(String content);

	/**
	 * Counts the number of tokens in the given {@link Document} after having been filtered
	 * for non-essential words.
	 *
	 * @param document {@link Document} containing the content used to count tokens.
	 * @return the number of token estimated from the given, filtered {@link Document}.
	 * @see #filteredTokenCount(String)
	 */
	default int filteredTokenCount(Document document) {
		return document != null ? filteredTokenCount(document.getContent()) : 0;
	}

	/**
	 * Counts the number of words in the given {@link String content}.
	 *
	 * @param content {@link String} containing the content used to count words.
	 * @return the number of words in the given {@link String content} or {@literal 0}
	 * if the {@link String content} is {@literal null}, {@literal empty} or {@literal blank}.
	 */
	default int wordCount(String content) {

		if (StringUtils.hasText(content)) {
			String singleSpacedContent = content.replaceAll("\\s+", " ");
			return singleSpacedContent.split(" ").length;
		}

		return 0;
	}
}
