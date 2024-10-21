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

import io.codeprimate.tools.spring.ai.tokens.model.TokenMetadata;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCostService;

import org.springframework.stereotype.Service;

/**
 * {@link TokenCostService} implementation based on OpenAI.
 *
 * @author John Blum
 * @see java.math.BigDecimal
 * @see TokenCostService
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@SuppressWarnings("unused")
public class OpenAITokenCostService implements TokenCostService {

	@Override
	public TokenMetadata cost(String modelName, int tokenCount) {
		return TokenMetadata.from(modelName).count(tokenCount).build();
	}
}
