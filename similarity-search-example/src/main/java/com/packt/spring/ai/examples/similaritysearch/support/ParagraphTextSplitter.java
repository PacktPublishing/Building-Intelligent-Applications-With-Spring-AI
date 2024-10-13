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

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

/**
 * {@link TextSplitter} splitting paragraphs in the {@link String text} of the content from the {@link Document}.
 * <p/>
 * Additionally, this implementation will transform the {@link String content (text)} from the {@link Document}
 * to lowercase and remove all non-essential words.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.similaritysearch.support.AbstractNonEssentialWordsPreProcessingTextSplitter
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ParagraphTextSplitter extends AbstractLowercasePreProcessingTextSplitter {

	@Override
	protected String regex() {
		return "\\v{2}";
	}
}
