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

import io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractBaseTextSplitter;
import io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCountEstimatorService;

import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} using the Spring AI's {@link TokenCountEstimator} and the {@literal JTokkit} library
 * to estimate the number of token from a given {@link String text content}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenCostEstimatorService
 * @see org.springframework.ai.tokenizer.TokenCountEstimator
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class JTokkitTokenCountEstimatorService implements TokenCountEstimatorService {

	private final AbstractBaseTextSplitter textSplitter = newTextSplitter();

	private final TokenCountEstimator tokenCountEstimator;

	protected AbstractBaseTextSplitter newTextSplitter() {
		return new DocumentTextSplitter();
	}

	@Override
	public int tokenCount(String content) {
		return getTokenCountEstimator().estimate(content);
	}

	@Override
	public int filteredTokenCount(String content) {
		String processedContent = getTextSplitter().preProcess(content);
		return tokenCount(processedContent);
	}
}
