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
package io.codeprimate.tools.spring.ai.tokens.service.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.codeprimate.tools.spring.ai.tokens.service.WordCountService;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link JTokkitTokenizerService}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see io.codeprimate.tools.spring.ai.tokens.service.provider.JTokkitTokenizerService
 * @since 0.1.0
 */
public class JTokkitTokenizerServiceUnitTests {

	private final JTokkitTokenizerService tokenizerService = new JTokkitTokenizerService();

	private final WordCountService wordCountService = WordCountService.defaultService();

	@Test
	void tokensAreCorrect() {

		String content = "The wonderful world of Artificial Intelligence (AI) is at our fingertips!";

		List<String> tokens = this.tokenizerService.tokenize(content);

		assertThat(tokens).isNotNull();
		assertThat(tokens).hasSizeGreaterThanOrEqualTo(this.wordCountService.countWords(content));

		//System.out.println(tokens);
	}
}
