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
package com.packt.spring.ai.examples.connect4.model;

import org.cp.elements.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Abstract Data Type (ADT) modeling an AI model's move and why it made the move.
 *
 * @author John Blum
 * @since 0.1.0
 */
public record Play(String move, String explanation) {

	public static final String DEFAULT_EXPLANATION = "?";

	public Play {
		Assert.hasText(move, "Move is required");
		explanation = StringUtils.defaultIfBlank(explanation, DEFAULT_EXPLANATION);
	}

	public static Play from(String move, String explanation) {
		return new Play(move, explanation);
	}
}
