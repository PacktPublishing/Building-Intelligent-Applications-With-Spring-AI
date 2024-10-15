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
package com.packt.spring.ai.examples.testing.pregen.model;

import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling an answer generated from AI.
 *
 * @author John Blum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public record Answer(String content) {

	public static final Answer UNKNOWN = new Answer("unknown");

	public Answer {
		Assert.hasText(content, "Content of Answer is required");
	}

	public static Answer from(String content) {
		return new Answer(content);
	}

	public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	@Override
	public String toString() {
		return content();
	}
}
