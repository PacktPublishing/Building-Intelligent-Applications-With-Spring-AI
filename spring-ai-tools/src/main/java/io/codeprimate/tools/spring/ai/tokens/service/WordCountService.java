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

import io.codeprimate.tools.spring.ai.tokens.service.provider.DefaultWordCountService;

/**
 * Service interface used to count the number of {@literal words} in a body of {@link String text}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenCountService
 * @since 0.1.0
 */
@FunctionalInterface
public interface WordCountService {

	/**
	 * Factory method to get the default implementation of the {@link WordCountService}.
	 *
	 * @return the default implementation of the {@link WordCountService}.
	 */
	static WordCountService defaultService() {
		return new DefaultWordCountService();
	}

	/**
	 * Counts the number of words in the given {@link String content}, such as a body of {@link String plaintext}.
	 *
	 * @param content {@link String} containing the content used to count words.
	 * @return the number of words in the given {@link String content}, or return {@literal 0}
	 * if the {@link String content} is {@literal null}, {@literal empty} or {@literal blank}.
	 */
	int countWords(String content);

}
