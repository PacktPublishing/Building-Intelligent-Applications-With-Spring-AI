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
package io.codeprimate.tools.spring.ai.tokens.config;

import com.knuddels.jtokkit.api.EncodingType;

import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootConfiguration} for the Token Cost Estimator application.
 *
 * @author John Blum
 * @see org.springframework.ai.tokenizer.JTokkitTokenCountEstimator
 * @see org.springframework.ai.tokenizer.TokenCountEstimator
 * @see org.springframework.boot.SpringBootConfiguration
 * @since 0.1.0
 */
@SpringBootConfiguration
@SuppressWarnings("unused")
public class TokenMetadataConfiguration {

	@Bean
	public TokenCountEstimator tokenCountEstimator(
			@Value("${app.token-count-estimator.tokenization.encoding-type:o200k_base}") String encodingType) {

		return new JTokkitTokenCountEstimator(EncodingType.fromName(encodingType)
			.orElseThrow(() -> new IllegalArgumentException("EncodingType [%s] not found".formatted(encodingType))));
	}
}
