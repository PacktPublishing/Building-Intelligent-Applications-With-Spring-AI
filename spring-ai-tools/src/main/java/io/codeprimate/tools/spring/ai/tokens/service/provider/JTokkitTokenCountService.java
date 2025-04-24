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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.stereotype.Service;

import io.codeprimate.extensions.spring.ai.transformer.splitter.AbstractBaseTextSplitter;
import io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCountService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring {@link Service} using Spring AI's {@link TokenCountEstimator} and the {@literal JTokkit} library
 * to estimate the number of token from the given {@link String content}, such as a body of {@link String plaintext}.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenCountService
 * @see org.springframework.ai.tokenizer.TokenCountEstimator
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class JTokkitTokenCountService implements TokenCountService {

	private final AbstractBaseTextSplitter textSplitter = newTextSplitter();

	private final TokenCountEstimator tokenCountEstimator;

	protected AbstractBaseTextSplitter newTextSplitter() {
		return DocumentTextSplitter.create().excludeNonEssentialWords();
	}

	@Override
	public int countTokens(String content) {
		return getTokenCountEstimator().estimate(content);
	}

	@Override
	public int countTokens(Media media) {
		return getTokenCountEstimator().estimate(newMediaContent(media));
	}

	@Override
	public int countFilteredTokens(String content) {
		String processedContent = getTextSplitter().preProcess(content);
		return countTokens(processedContent);
	}

	protected MediaContent newMediaContent(Media media) {

		return new MediaContent() {

			@Override
			public List<Media> getMedia() {
				return Collections.singletonList(media);
			}

			@Override
			public Map<String, Object> getMetadata() {
				return Collections.emptyMap();
			}

			@Override
			public String getText() {
				return null;
			}
		};
	}
}
