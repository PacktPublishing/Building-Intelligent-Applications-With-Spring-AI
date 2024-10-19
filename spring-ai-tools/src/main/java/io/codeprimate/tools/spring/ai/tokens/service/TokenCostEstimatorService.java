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
package io.codeprimate.tools.spring.ai.tokens.service;

import java.math.BigDecimal;

/**
 * Service interface used to compute the cost for a given number of tokens when using a particular AI provider model.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface TokenCostEstimatorService {

	/**
	 * Computes the cost for the given {@link Integer number} of tokens when using the given,
	 * {@link String named} AI provider model
	 *
	 * @param modelName {@link String} containing the name of the AI provide model (e.g. {@literal OpenAI gpt-4o)).
	 * @param tokenCount {@link Integer} specifying the number of tokens used.
	 * @return a {@link BigDecimal} indicating the cost in {@literal USD} for the given number of tokens
	 * when using the {@link String named} AI provider model.
	 * @see java.math.BigDecimal
	 */
	BigDecimal cost(String modelName, int tokenCount);

}
