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
package io.codeprimate.tools.spring.ai.tokens.model;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) and Java record used to model {@link String content} such as plain text.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
public record Document(String content) {

	public static Document from(String content) {
		Assert.hasText(content, () -> "Content [%s] is required".formatted(content));
		return new Document(content);
	}

	public static Document from(org.springframework.ai.document.Document document) {
		Assert.notNull(document, "Document is required");
		return new Document(document.getContent());
	}

	@Override
	public String toString() {
		return content();
	}
}
