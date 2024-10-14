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
package com.packt.spring.ai.examples.similaritysearch.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * Abstract base class removing all non-essential words in a given {@link String text}.
 * <p/>
 * This includes {@literal Articles}, {@literal Linking Verbs} and {@literal Prepositions}
 * along with all {@literal Punctuation}.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.similaritysearch.support.AbstractLowercasePreProcessingTextSplitter
 * @since 0.1.0
 */
public abstract class AbstractNonEssentialWordsPreProcessingTextSplitter
		extends AbstractLowercasePreProcessingTextSplitter {

	protected static final Set<String> ARTICLES = Set.of("a", "an", "the");

	protected static final Set<String> CONJUNCTIONS = Set.of("and", "or");

	// Also known as Copular and Auxiliary Linking Verbs; Not an exhaustive Set.
	protected static final Set<String> LINKING_VERBS =
		Set.of("am", "are", "be", "became", "become", "becomes", "been", "being", "do", "did", "does", "had",
			"has", "have", "is", "seem", "seems", "was", "will", "would");

	// Not an exhaustive Set.
	protected static final Set<String> PREPOSITIONS = Set.of("at", "by", "from", "of", "to");

	protected static final String PUNCTUATION_REGEX = "\\p{Punct}";

	protected Set<String> getAdditionalNonEssentialWords() {
		return Collections.emptySet();
	}

	protected Set<String> getArticles() {
		return ARTICLES;
	}

	protected Set<String> getConjunctions() {
		return CONJUNCTIONS;
	}

	protected Set<String> getLinkingVerbs() {
		return LINKING_VERBS;
	}

	protected Set<String> getPrepositions() {
		return PREPOSITIONS;
	}

	protected Optional<String> getPunctuationRegex() {
		return Optional.of(PUNCTUATION_REGEX);
	}

	protected Set<String> getNonEssentialWords() {

		Set<String> nonEssentialWords = new HashSet<>();

		nonEssentialWords.addAll(getArticles());
		nonEssentialWords.addAll(getConjunctions());
		nonEssentialWords.addAll(getLinkingVerbs());
		nonEssentialWords.addAll(getPrepositions());
		nonEssentialWords.addAll(getAdditionalNonEssentialWords());

		return nonEssentialWords;
	}

	private ImmutableSetWrapper<String> getWrappedNonEssentialWords() {
		return ImmutableSetWrapper.from(getNonEssentialWords());
	}

	@Override
	public String preProcess(String text) {

		String preProcessedText = super.preProcess(text);

		String preProcessedTextWithNoPunctuation = getPunctuationRegex()
			.filter(StringUtils::hasText)
			.map(punctuationRegex -> preProcessedText.replaceAll(punctuationRegex, EMPTY_STRING))
			.orElse(preProcessedText);

		String[] preProcessedTextWords = preProcessedTextWithNoPunctuation.split(WHITESPACE_REGEX);

		List<String> preProcessedTextWordList = new ArrayList<>(List.of(preProcessedTextWords));

		getWrappedNonEssentialWords().ifNotEmpty(preProcessedTextWordList::removeAll);

		return preProcessedTextWordList.stream()
			.map(SINGLE_SPACE::concat)
			.reduce(String::concat)
			.map(String::trim)
			.orElse(EMPTY_STRING);
	}
}
