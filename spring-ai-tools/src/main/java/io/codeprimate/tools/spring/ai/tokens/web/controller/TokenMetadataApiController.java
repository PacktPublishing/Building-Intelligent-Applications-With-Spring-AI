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

import io.codeprimate.tools.spring.ai.tokens.model.Document;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCostService;
import io.codeprimate.tools.spring.ai.tokens.service.TokenCountService;
import io.codeprimate.tools.spring.ai.tokens.service.WordCountService;

import org.springframework.ai.model.Media;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link RestController} used to provide token count and cost estimates.
 *
 * @author John Blum
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenCostService
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenCountService
 * @see io.codeprimate.tools.spring.ai.tokens.service.WordCountService
 * @see io.codeprimate.tools.spring.ai.tokens.web.controller.AbstractApiBaseController
 * @see org.springframework.ai.model.Media
 * @see org.springframework.core.io.Resource
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.multipart.MultipartFile
 * @since 0.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class TokenMetadataApiController extends AbstractApiBaseController {

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

	@PostMapping("/count/media")
	public CountMetadata tokenCount(MultipartFile file) {

		MimeType mimeType = resolveMimeType(file);
		Resource resource = resolveResource(file);
		Media media = new Media(mimeType, resource);

		int tokenCount = getTokenCountService().countTokens(media);

		return CountMetadata.builder()
			.tokenCount(tokenCount)
			.noFilteredTokens()
			.noWords()
			.build();
	}

	@GetMapping("/generation/cost")
	public CostMetadata tokenGenerationCost(@RequestParam("modelName") String modelName, @RequestParam("count") int count) {
		return CostMetadata.generation(getTokenCostService().cost(modelName, count));
	}

	@GetMapping("/prompt/cost")
	public CostMetadata tokenPromptCost(@RequestParam("modelName") String modelName, @RequestParam("count") int count) {
		return CostMetadata.prompt(getTokenCostService().cost(modelName, count));
	}
}
