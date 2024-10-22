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
 * Unit Tests for {@link AbstractLowercasePreProcessingTextSplitter}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractLowercasePreProcessingTextSplitter
 * @since 0.1.0
 */
public class AbstractLowercasePreProcessingTextSplitterUnitTests {

	private final AbstractLowercasePreProcessingTextSplitter textSplitter
		= new TestLowercasePreProcessingTextSplitter();

	@Test
	void preProcessesTextCorrectly() {

		String text = " tHIs is A StrING of TeXT!  ";

		String expectedText = text.toLowerCase().trim();
		String actualText = this.textSplitter.preProcess(text);

		assertThat(actualText).isNotBlank();
		assertThat(actualText).isEqualTo(expectedText);

	}

	static final class TestLowercasePreProcessingTextSplitter extends AbstractLowercasePreProcessingTextSplitter {

		@Override
		public String regex() {
			throw new UnsupportedOperationException("Not Implemented");
		}
	}
}
