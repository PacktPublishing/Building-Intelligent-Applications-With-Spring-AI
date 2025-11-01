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
import java.util.List;

import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

/**
 * Abstract base class encapsulating functionality supporting implementations of Spring AI's {@link TextSplitter}.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.ai.transformer.splitter.TextSplitter
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractBaseTextSplitter extends TextSplitter {

	protected static final String EMPTY_STRING = Utils.EMPTY_STRING;
	protected static final String MATCH_ALL_REGEX = ".*";
	protected static final String NEWLINE = System.lineSeparator();
	protected static final String SINGLE_SPACE = Utils.SINGLE_SPACE;
	protected static final String VERTICAL_WHITESPACE_REGEX = "\\v";
	protected static final String WHITESPACE_REGEX = "\\s+";

	public String preProcess(String text) {
		return text.trim();
	}

	public abstract String regex();

	public List<Document> split(String text) {
		return split(new Document(text));
	}

	@Override
	protected List<String> splitText(String text) {

		return StringUtils.hasText(text)
			? doSplitText(preProcess(text), regex())
			: Collections.emptyList();
	}

	private List<String> doSplitText(String text, String regex) {

		return StringUtils.hasText(regex)
			? Arrays.asList(text.split(regex))
			: Collections.singletonList(text);
	}
}
