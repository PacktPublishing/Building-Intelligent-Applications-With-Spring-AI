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
package io.codeprimate.tools.spring.ai.tokens.web.controller;

import java.math.BigDecimal;

import io.codeprimate.tools.spring.ai.tokens.service.TokenCostEstimatorService;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCountEstimatorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link RestController} used to provide token count and cost estimates.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @since 0.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class TokenCostEstimatorApiController {

	private final TokenCostEstimatorService tokenCostEstimatorService;

	private final TokenCountEstimatorService tokenCountEstimatorService;

	@PostMapping("/tokens/count")
	public TokenCount tokenCount(@RequestBody Document document) {

		TokenCountEstimatorService service = getTokenCountEstimatorService();

		String content = document.content();

		//log.info("Document Content [{}]", content);

		return TokenCount.builder()
			.wordCount(service.wordCount(content))
			.tokenCount(service.tokenCount(content))
			.filteredTokenCount(service.filteredTokenCount(content))
			.build();
	}

	@GetMapping("/tokens/cost")
	public TokenCost tokenCost(@RequestParam("modelName") String modelName, @RequestParam("count") int count) {

		return TokenCost.builder()
			.cost(getTokenCostEstimatorService().cost(modelName, count))
			.build();
	}

	public record Document(String content) {

		@Override
		public String toString() {
			return content();
		}
	}

	@Getter
	@Builder
	public static class TokenCost {
		private BigDecimal cost;
	}

	@Getter
	@Builder
	public static class TokenCount {
		private int wordCount;
		private int tokenCount;
		private int filteredTokenCount;
	}
}
