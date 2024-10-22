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
 * Unit Tests for {@link AbstractPunctuationPreProcessingTextSplitter}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractPunctuationPreProcessingTextSplitter
 * @since 0.1.0
 */
public class AbstractPunctuationPreProcessingTextSplitterUnitTests {

	private final AbstractPunctuationPreProcessingTextSplitter textSplitter
		= new TestPunctuationPreProcessingTextSplitter();

	@Test
	void removesPunctuationFromText() {

		String text = "This is an example sentence. Counting down from: three, two, one... and BOOM!"
			+ "\nDid you hear that?";

		String expectedText = "This is an example sentence Counting down from three two one and BOOM"
			+ "\nDid you hear that";

		String actualText = this.textSplitter.preProcess(text);

		assertThat(actualText).isEqualTo(expectedText);
	}

	static class TestPunctuationPreProcessingTextSplitter extends AbstractPunctuationPreProcessingTextSplitter {

		@Override
		public String regex() {
			return null;
		}
	}
}
