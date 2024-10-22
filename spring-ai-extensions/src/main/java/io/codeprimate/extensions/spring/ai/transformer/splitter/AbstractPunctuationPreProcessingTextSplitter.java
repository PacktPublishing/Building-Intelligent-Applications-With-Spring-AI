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
package io.codeprimate.extensions.spring.ai.transformer.splitter;

import java.util.Optional;

import org.springframework.util.StringUtils;

/**
 * Abstract base class removing all punctuation from a body of {@link String plaintext}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractLowercasePreProcessingTextSplitter
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractPunctuationPreProcessingTextSplitter extends AbstractLowercasePreProcessingTextSplitter {

	protected static final String PUNCTUATION_REGEX = "\\p{Punct}";

	protected Optional<String> getPunctuationRegex() {
		return Optional.of(PUNCTUATION_REGEX);
	}

	@Override
	@SuppressWarnings("all")
	public String preProcess(String text) {

		String preProcessedText =  super.preProcess(text);
		String preProcessedTextNoPunctuation = removePunctuation(preProcessedText);

		return preProcessedTextNoPunctuation;
	}

	protected String removePunctuation(String text) {

		return getPunctuationRegex()
			.filter(StringUtils::hasText)
			.map(punctuationRegex -> text.replaceAll(punctuationRegex, EMPTY_STRING))
			.orElse(text);
	}
}
