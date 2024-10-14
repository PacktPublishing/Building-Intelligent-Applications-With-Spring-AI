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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests in {@link NewlineTextSplitter}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see com.packt.spring.ai.examples.similaritysearch.support.NewlineTextSplitter
 * @since 0.1.0
 */
public class NewlineTextSplitterUnitTests {

	private final NewlineTextSplitter textSplitter = new NewlineTextSplitter();

	@Test
	void splitsTextCorrect() {

		String text = " The first line.  \nThe second line. \n\n\n   The third line.";

		List<String> expectedText = Arrays.asList("first line\nsecond line\nthird line".split("\\v+"));
		List<String> actualTexts = this.textSplitter.splitText(text);

		assertThat(actualTexts).isNotNull();
		assertThat(actualTexts).hasSize(3);
		assertThat(actualTexts).isEqualTo(expectedText);
	}
}
