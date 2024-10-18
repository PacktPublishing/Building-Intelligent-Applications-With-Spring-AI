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
 * Service component used to compute the cost of tokens used for a particular AI provider and AI model.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface TokenCostEstimatorService {

	BigDecimal cost(String modelName, int tokenCount);

}
