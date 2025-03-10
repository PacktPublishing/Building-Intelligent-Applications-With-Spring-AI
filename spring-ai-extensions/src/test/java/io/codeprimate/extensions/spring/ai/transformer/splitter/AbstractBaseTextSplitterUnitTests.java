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
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

/**
 * Unit Tests for {@link AbstractBaseTextSplitter}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.junit.jupiter.MockitoExtension
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractBaseTextSplitter
 * @since 0.1.0
 */
@ExtendWith(MockitoExtension.class)
public class AbstractBaseTextSplitterUnitTests {

	@Spy
	private final AbstractBaseTextSplitter textSplitter = new TestBaseTextSplitter();

	@Test
	void preProcessTrimsText() {

		assertThat(this.textSplitter.preProcess("test")).isEqualTo("test");
		assertThat(this.textSplitter.preProcess(" test  ")).isEqualTo("test");
		assertThat(this.textSplitter.preProcess(" 1 2  3   ")).isEqualTo("1 2  3");
	}

	@Test
	void splitDocument() {

		List<Document> documents = this.textSplitter.split("test");

		assertThat(documents).isNotNull();
		assertThat(documents).hasSize(1);
		assertThat(documents.get(0).getText()).isEqualTo("test");
	}

	@Test
	void splitNoText() {
		Arrays.asList("  ", "", null).forEach(text ->
			assertThat(this.textSplitter.splitText(text)).isNotNull().isEmpty());
	}

	@Test
	void splitTextReturnsText() {
		assertThat(this.textSplitter.splitText("one two"))
			.isNotNull().hasSize(1).containsExactly("one two");
	}

	@Test
	void splitTextSplitsByWhitespace() {

		doReturn(AbstractBaseTextSplitter.WHITESPACE_REGEX).when(this.textSplitter).regex();

		assertThat(this.textSplitter.splitText("one two"))
			.isNotNull().hasSize(2).containsExactly("one", "two");
	}

	static class TestBaseTextSplitter extends AbstractBaseTextSplitter {

		@Override
		public String regex() {
			return null;
		}
	}
}
