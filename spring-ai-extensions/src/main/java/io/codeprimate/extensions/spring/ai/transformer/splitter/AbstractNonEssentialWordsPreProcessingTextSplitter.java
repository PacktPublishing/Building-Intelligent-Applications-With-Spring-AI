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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class removing all non-essential words in a given body of {@link String plaintext}.
 * <p/>
 * This includes {@literal Articles}, {@literal Linking Verbs} and {@literal Prepositions}
 * along with all {@literal Punctuation}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractPunctuationPreProcessingTextSplitter
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractNonEssentialWordsPreProcessingTextSplitter
		extends AbstractPunctuationPreProcessingTextSplitter {

	protected static final String MULTI_SPACED_WORDS_REGEX = " {2,}";
	protected static final String NON_ESSENTIAL_WORD_REGEX_TEMPLATE = "\\b%s\\b";

	// Capitalized words represent the word used at the beginning of sentence.
	// Lowercase words represent the word in the middle of a sentence.
	protected static final Set<String> ARTICLES = Set.of("The", "the");

	// Capitalized variant represents the word used at the beginning of sentence.
	// Lowercase words represent the word in the middle or end of a sentence.
	protected static final Set<String> ADVERBIAL_CONJUNCTIONS =
		Set.of("Also", "also", "Anyway", "anyway", "Furthermore", "furthermore", "Hence", "hence", "However", "however",
			"Indeed", "indeed", "Likewise", "likewise", "Moreover", "moreover", "Nevertheless", "nevertheless",
			"Of course", "of course", "Therefore", "therefore", "Thus", "thus");

	// Capitalized words represent the word used at the beginning of sentence.
	// Lowercase words represent the word in the middle or end of a sentence.
	protected static final Set<String> COORDINATING_CONJUNCTIONS =
		Set.of("And", "But", "but", "Or", "So", "so", "Yet", "yet");

	protected static final BinaryOperator<String> TWO_STRINGS_NEWLINE_CONCATENATION = (one, two) ->
		one.concat(NEWLINE).concat(two);

	protected Set<String> getAdditionalNonEssentialWords() {
		return Collections.emptySet();
	}

	protected Set<String> getArticles() {
		return ARTICLES.stream().filter(articlesPredicate()).collect(Collectors.toSet());
	}

	protected Predicate<String> articlesPredicate() {
		return article -> true;
	}

	protected Set<String> getConjunctions() {

		return Stream.concat(ADVERBIAL_CONJUNCTIONS.stream(), COORDINATING_CONJUNCTIONS.stream())
			.filter(conjunctionsPredicate())
			.collect(Collectors.toSet());
	}

	protected Predicate<String> conjunctionsPredicate() {
		return conjunction -> true;
	}

	protected Set<String> getNonEssentialWords() {

		Set<String> nonEssentialWords = new HashSet<>();

		nonEssentialWords.addAll(getArticles());
		nonEssentialWords.addAll(getConjunctions());
		nonEssentialWords.addAll(getAdditionalNonEssentialWords());

		return nonEssentialWords.stream()
			.filter(nonEssentialWordsPredicate())
			.collect(Collectors.toSet());
	}

	protected Predicate<String> nonEssentialWordsPredicate() {
		return nonEssentialWord -> true;
	}

	@Override
	@SuppressWarnings("all")
	public String preProcess(String text) {

		String conciseText = removeNonEssentialWords(text);
		String preProcessedText = super.preProcess(conciseText);

		return preProcessedText;
	}

	protected String removeNonEssentialWords(String text) {

		for (String nonEssentialWord : getNonEssentialWords()) {
			String nonEssentialWordRegex = NON_ESSENTIAL_WORD_REGEX_TEMPLATE.formatted(nonEssentialWord);
			text = text.replaceAll(nonEssentialWordRegex, EMPTY_STRING)
				.replaceAll(MULTI_SPACED_WORDS_REGEX, SINGLE_SPACE)
				.trim();
		}

		return Arrays.stream(text.split(VERTICAL_WHITESPACE_REGEX))
			.map(String::trim)
			.reduce(TWO_STRINGS_NEWLINE_CONCATENATION)
			.orElse(text);
	}
}
