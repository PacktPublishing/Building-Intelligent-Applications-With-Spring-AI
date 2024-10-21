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

import java.io.IOException;
import java.math.BigDecimal;

import io.codeprimate.tools.spring.ai.tokens.model.TokenMetadata;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract base class for {@link RestController REST API Controllers}.
 *
 * @author John Blum
 * @since 0.1.0
 */
public abstract class AbstractApiBaseController {

	protected String assertContentType(String contentType) {
		Assert.hasText(contentType, () -> "Content-type [%s] is required");
		return contentType;
	}

	@SuppressWarnings("all")
	protected MimeType resolveMimeType(MultipartFile file) {
		return new MimeType(assertContentType(file.getContentType()));
	}

	protected Resource resolveResource(MultipartFile file) {

		try {
			return new ByteArrayResource(file.getBytes(), "Contents of [%s]".formatted(file.getName()));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
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

		@SuppressWarnings("all")
		public static class CountMetadataBuilder {

			private int filteredTokenCount;
			private int wordCount;

			public CountMetadataBuilder noFilteredTokens() {
				this.filteredTokenCount = 0;
				return this;
			}

			public CountMetadataBuilder noWords() {
				this.wordCount = 0;
				return this;
			}
		}

		private int filteredTokenCount;
		private int tokenCount;
		private int wordCount;

	}
}
