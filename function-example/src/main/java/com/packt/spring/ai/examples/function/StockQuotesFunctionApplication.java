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
package com.packt.spring.ai.examples.function;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import lombok.Getter;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama ({@literal llama3.2} model) to demonstrate Tool Calling
 * (Function Calling) in order to return a {@link StockQuote} given a stock (exchange) symbol,
 * for examples: {@literal AAPL}.
 * <p/>
 * Uses Polygon.io's REST API to fetch realtime stock market data, such as stock quotes.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.chat.prompt.ChatOptions
 * @see org.springframework.ai.chat.prompt.Prompt
 * @see org.springframework.ai.chat.prompt.PromptTemplate
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.core.env.Environment
 * @see org.springframework.web.client.RestClient
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class StockQuotesFunctionApplication {

	private static final String DEBUG_PROFILE = "debug";
	private static final String EXIT = "exit";
	private static final String STOCK_QUOTE_FUNCTION_NAME = "StockQuoteFunction";
	private static final String USER_PROFILE = "user";

	public static void main(String[] args) {

		new SpringApplicationBuilder(StockQuotesFunctionApplication.class)
			.web(WebApplicationType.NONE)
			.profiles(USER_PROFILE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	RestClient polygonClient(Environment environment) {

		String baseUrl = environment.getRequiredProperty("examples.app.stock-quotes.polygon.api.base-url");

		Consumer<HttpHeaders> defaultHttpHeaders = httpHeaders -> {
			String polygonApiKey = environment.getRequiredProperty("examples.app.stock-quotes.polygon.api.key");
			httpHeaders.setBearerAuth(polygonApiKey);
		};

		return RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeaders(defaultHttpHeaders)
			.build();
	}

	@Bean
	@Profile(DEBUG_PROFILE)
	RestClientCustomizer springBootRestClientCustomizer(ObjectMapper objectMapper) {

		return restClientBuilder -> restClientBuilder.requestInterceptor((request, requestBody, execution) -> {
			ClientHttpResponse response = execution.execute(request, requestBody);
			InputStream responseBody = response.getBody();
			String json = objectMapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(objectMapper.readTree(StreamUtils.nonClosing(responseBody)));
			print("AI Model Response in JSON [%s]%n", json);
			return response;
		});
	}

	@SpringBootConfiguration
	static class StockMarketTools {

		@Bean
		public ToolCallback stockQuote(Environment environment, RestClient polygonClient) {
			return FunctionToolCallback.builder(STOCK_QUOTE_FUNCTION_NAME, stockQuoteFunction(environment, polygonClient))
				.description("Get the current price for a stock")
				.inputType(StockQuote.Request.class)
				.build();
		}

		Function<StockQuote.Request, StockQuote.Response> stockQuoteFunction(Environment environment,
				RestClient polygonClient) {

			return request -> {

				String exchangeSymbol = request.exchangeSymbol();

				print("Fetching quote for stock [%s]...%n", exchangeSymbol);

				Map<String, ?> uriVariables = Map.of("exchangeSymbol", exchangeSymbol);

				String uri = environment
					.getRequiredProperty("examples.app.stock-quotes.polygon.api.market-data.previous-close.uri");

				StockQuote stockQuote = polygonClient.get()
					.uri(uri, uriVariables)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(StockQuote.class);

				Assert.state(stockQuote != null, () -> "Failed to retrieve quote for stock [%s]".formatted(request));

				return new StockQuote.Response(stockQuote.getFirstResult().getClose());
			};
		}
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient, ToolCallback stockQuote) {

		return args -> {

			Scanner scanner = new Scanner(System.in);
			String stockSymbol;

			print("Enter Stock Symbol: ");

			while (isNotExit(stockSymbol = scanner.nextLine())) {
				if (StringUtils.hasText(stockSymbol)) {

					String promptTemplate = "What is the current price for {stock}?";

					Map<String, Object> promptArguments = Map.of("stock", stockSymbol);

					Prompt prompt = new PromptTemplate(promptTemplate).create(promptArguments);

					String stockPrice = chatClient.prompt(prompt)
						.tools(stockQuote)
						.call()
						.content();

					print("AI> %s%n%n", stockPrice);
				}

				print("Enter Stock Symbol: ");
			}
		};
	}

	private boolean isNotExit(String input) {
		return StringUtils.hasText(input) && !EXIT.equalsIgnoreCase(input);
	}

	private static void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	@Getter
	public static class StockQuote {

		private Boolean adjusted;

		private Integer count;
		private Integer queryCount;
		private Integer resultsCount;

		@JsonProperty("request_id")
		private String requestId;

		private String status;
		private String ticker;

		@SuppressWarnings("all")
		private List<Result> results = Collections.emptyList();

		Result getFirstResult() {

			boolean hasResults = resultsCount != null
				&& resultsCount != 0
				&& !CollectionUtils.isEmpty(results);

			Assert.state(hasResults, () -> "Expected results in StockQuote [%s]".formatted(this));

			return getResults().get(0);
		}

		@Override
		public String toString() {
			return getTicker();
		}

		public record Request(String exchangeSymbol) {

		}

		public record Response(BigDecimal price) {

		}

		@Getter
		static class Result {

			@JsonProperty("o")
			private BigDecimal open;

			@JsonProperty("c")
			private BigDecimal close;

			@JsonProperty("h")
			private BigDecimal high;

			@JsonProperty("l")
			private BigDecimal low;

			@JsonProperty("vw")
			private BigDecimal volumeWeightAveragePrice;

			@JsonProperty("n")
			private BigInteger numberOfTransactions;

			@JsonProperty("v")
			private BigInteger volume;

			@JsonProperty("t")
			private Instant timestamp;

			@JsonProperty("T")
			private String ticker;

		}
	}
}
