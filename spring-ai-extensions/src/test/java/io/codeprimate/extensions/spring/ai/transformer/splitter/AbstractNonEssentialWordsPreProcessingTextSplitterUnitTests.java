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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link AbstractNonEssentialWordsPreProcessingTextSplitter}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractNonEssentialWordsPreProcessingTextSplitter
 * @since 0.1.0
 */
public class AbstractNonEssentialWordsPreProcessingTextSplitterUnitTests {

	private final AbstractNonEssentialWordsPreProcessingTextSplitter textSplitter
		= new TestNonEssentialWordsPreProcessingTextSplitter();

	@Test
	@SuppressWarnings("all")
	void preProcessesTextCorrectly() {

		String text = "  This is a sentence!"
			+ "\nAnd, this is yet another sentence I did?"
			+ "\n\nWe  have all   sorts of sentences it seems. "
			+ "\n\n\nMounds and mounds of sentences or phrases.  Or other things!";

		String expectedText = "this is a sentence\nthis is another sentence i did"
			+ "\n\nwe have all sorts of sentences it seems\n\n\nmounds and mounds of sentences or phrases other things";

		String actualText = this.textSplitter.preProcess(text);

		assertThat(actualText).isNotBlank();

		String[] expectedTexts = expectedText.split("\\v");
		String[] actualTexts = actualText.split("\\v");

		assertThat(expectedTexts).hasSize(7);
		assertThat(actualTexts).isEqualTo(expectedTexts);
	}

	static class TestNonEssentialWordsPreProcessingTextSplitter
			extends AbstractNonEssentialWordsPreProcessingTextSplitter {

		@Override
		public String regex() {
			throw new UnsupportedOperationException("Not Implemented");
		}
	}
}
