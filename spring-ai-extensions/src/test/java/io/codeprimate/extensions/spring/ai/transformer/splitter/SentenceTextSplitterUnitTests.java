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

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link SentenceTextSplitter}
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.SentenceTextSplitter
 * @since 0.1.0
 */
public class SentenceTextSplitterUnitTests {

	private final SentenceTextSplitter textSplitter = new SentenceTextSplitter();

	@Test
	void preProcessesTextCorrectly() {

		String text = "Text containing a, comma.";
		String processedText = this.textSplitter.preProcess(text);

		assertThat(processedText).isNotBlank();
		assertThat(processedText).isEqualTo("text containing comma.");
	}

	@Test
	void splitBlankText() {

		String text = "  ";

		List<String> sentences = this.textSplitter.splitText(text);

		assertThat(sentences).isNotNull().isEmpty();
	}

	@Test
	void splitEmptyText() {

		String text = "";

		List<String> sentences = this.textSplitter.splitText(text);

		assertThat(sentences).isNotNull().isEmpty();
	}

	@Test
	void splitNullTest() {

		List<String> sentences = this.textSplitter.splitText(null);

		assertThat(sentences).isNotNull().isEmpty();
	}

	@Test
	void splitsNoSentenceText() {

		String text = "This is an incomplete sentence And, this is another incomplete sentence";

		List<String> sentences = this.textSplitter.splitText(text);

		assertThat(sentences).isNotNull();
		assertThat(sentences).hasSize(1);
		assertThat(sentences.get(0))
			.isEqualTo("this incomplete sentence this another incomplete sentence");
	}

	@Test
	void splitsTwoSentenceText() {

		String text = "This is the first sentence. Is this the second sentence?";

		List<String> sentences = this.textSplitter.splitText(text);

		assertThat(sentences).isNotNull();
		assertThat(sentences).hasSize(2);
		assertThat(sentences.get(0)).isEqualTo("this first sentence.");
		assertThat(sentences.get(1)).isEqualTo("this second sentence?");
	}

	@Test
	void splitsThreeSentenceTextCorrectly() {

		String text = "This is a test! What is a test? A test is a test.";

		List<String> sentences = this.textSplitter.splitText(text);

		assertThat(sentences).isNotNull();
		assertThat(sentences).hasSize(3);
		assertThat(sentences.get(0)).isEqualTo("this test!");
		assertThat(sentences.get(1)).isEqualTo("what test?");
		assertThat(sentences.get(2)).isEqualTo("test test.");
	}
}
