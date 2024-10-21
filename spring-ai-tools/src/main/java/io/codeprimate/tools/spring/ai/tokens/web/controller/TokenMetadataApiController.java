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

import io.codeprimate.tools.spring.ai.tokens.model.Document;
import io.codeprimate.tools.spring.ai.tokens.model.TokenMetadata;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCostService;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCountService;
import io.codeprimate.tools.spring.ai.tokens.service.WordCountService;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class TokenMetadataApiController {

	private final TokenCostService tokenCostService;

	private final TokenCountService tokenCountService;

	private final WordCountService wordCountService;

	@PostMapping("/count")
	public CountMetadata tokenCount(@RequestBody Document document) {

		TokenCountService tokenCountService = getTokenCountService();

		WordCountService wordCountService = getWordCountService();

		String content = document.content();

		//log.info("Document Content [{}]", content);

		return CountMetadata.builder()
			.wordCount(wordCountService.countWords(content))
			.tokenCount(tokenCountService.countTokens(content))
			.filteredTokenCount(tokenCountService.countFilteredTokens(content))
			.build();
	}

	@GetMapping("/prompt/cost")
	public CostMetadata tokenPromptCost(@RequestParam("modelName") String modelName, @RequestParam("count") int count) {
		return CostMetadata.prompt(getTokenCostService().cost(modelName, count));
	}

	@GetMapping("/generation/cost")
	public CostMetadata tokenGenerationCost(@RequestParam("modelName") String modelName, @RequestParam("count") int count) {
		return CostMetadata.generation(getTokenCostService().cost(modelName, count));
	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class CostMetadata {

		private static CostMetadata from(TokenMetadata.Cost cost) {

			return CostMetadata.builder()
				.amount(cost.getAmount())
				.currencyCode(cost.getCurrencyCode())
				.build();
		}

		public static CostMetadata generation(TokenMetadata tokenMetadata) {

			Assert.notNull(tokenMetadata, "TokenMetadata is required");

			return from(tokenMetadata.getGenerationCost());
		}

		public static CostMetadata prompt(TokenMetadata tokenMetadata) {

			Assert.notNull(tokenMetadata, "TokenMetadata is required");

			return from(tokenMetadata.getPromptCost());
		}

		private BigDecimal amount;

		private String currencyCode;

	}

	@Getter
	@Builder
	public static class CountMetadata {

		private int filteredTokenCount;
		private int tokenCount;
		private int wordCount;

	}
}
