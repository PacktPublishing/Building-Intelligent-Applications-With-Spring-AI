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
package io.codeprimate.tools.spring.ai.tokens.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import io.codeprimate.extensions.util.Utils;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Abstract Data Type (ADT) modeling the metadata for tokens using a given named AI model.
 *
 * @author John Blum
 * @see java.math.BigDecimal
 * @see java.util.Currency
 * @see java.util.Locale
 * @since 0.1.0
 */
@Getter
@Builder
@SuppressWarnings("unused")
public class TokenMetadata {

	protected static final int DEFAULT_COUNT = 0;

	public static TokenMetadataBuilder copy(TokenMetadata tokenMetadata) {

		Assert.notNull(tokenMetadata, "TokenMetadata to copy is required");

		return TokenMetadata.builder()
			.modelName(tokenMetadata.getModelName())
			.promptCost(tokenMetadata.getPromptCost())
			.generationCost(tokenMetadata.getGenerationCost())
			.count(tokenMetadata.getCount())
			.filteredCount(tokenMetadata.getFilteredCount())
			.wordCount(tokenMetadata.getWordCount());
	}

	public static TokenMetadataBuilder from(String modelName) {

		Assert.hasText(modelName, () -> "Name of model [%s] is required".formatted(modelName));

		return TokenMetadata.builder().modelName(modelName);
	}

	@Builder.Default
	private Cost generationCost = Cost.zero(Type.GENERATION);

	@Builder.Default
	private Cost promptCost = Cost.zero(Type.PROMPT);

	@Builder.Default
	private int count = DEFAULT_COUNT;

	@Builder.Default
	private int filteredCount = DEFAULT_COUNT;

	@Builder.Default
	private int wordCount = DEFAULT_COUNT;

	private String modelName;

	public TokenMetadataBuilder mutate() {
		return copy(this);
	}
	@Getter
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class Cost {

		protected static final BigDecimal DEFAULT_AMOUNT = BigDecimal.ZERO;

		protected static final Currency DEFAULT_CURRENCY = Currency.getInstance(Locale.getDefault());

		protected static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

		static {
			CURRENCY_FORMAT.setMinimumFractionDigits(8);
			CURRENCY_FORMAT.setMinimumIntegerDigits(1);
			CURRENCY_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		}

		public static Cost generation(BigDecimal amount) {
			return generation(amount, DEFAULT_CURRENCY);
		}

		public static Cost generation(BigDecimal amount, Currency currency) {
			return new Cost(nullSafeAmount(amount), nullSafeCurrency(currency), Type.GENERATION);
		}

		public static Cost prompt(BigDecimal amount) {
			return prompt(amount, DEFAULT_CURRENCY);
		}

		public static Cost prompt(BigDecimal amount, Currency currency) {
			return new Cost(nullSafeAmount(amount), nullSafeCurrency(currency), Type.PROMPT);
		}

		public static Cost zero(Type type) {

			Assert.notNull(type, "Token Type is required");

			return switch (type) {
				case PROMPT -> prompt(BigDecimal.ZERO);
				case GENERATION -> generation(BigDecimal.ZERO);
			};
		}

		private static BigDecimal nullSafeAmount(BigDecimal value) {
			return Utils.defaultIfNull(value, DEFAULT_AMOUNT);
		}

		private static Currency nullSafeCurrency(Currency currency) {
			return Utils.defaultIfNull(currency, DEFAULT_CURRENCY);
		}

		private final BigDecimal amount;

		private final Currency currency;

		private final Type type;

		public boolean isGeneration() {
			return Type.GENERATION.equals(getType());
		}

		public boolean isPrompt() {
			return Type.PROMPT.equals(getType());
		}

		public String getCurrencyCode() {
			return getCurrency().getCurrencyCode();
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Cost that)) {
				return false;
			}

			return this.getAmount().equals(that.getAmount())
				&& this.getCurrency().equals(that.getCurrency());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getAmount(), getCurrency());
		}

		@Override
		public String toString() {
			return CURRENCY_FORMAT.format(getAmount().doubleValue());
		}
	}

	public enum Type {
		PROMPT, GENERATION
	}
}
