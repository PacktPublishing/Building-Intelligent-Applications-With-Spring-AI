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
package com.packt.spring.ai.examples.travel.provider.google.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.packt.spring.ai.examples.travel.provider.google.api.GoogleFlightsApi;
import com.packt.spring.ai.examples.travel.provider.google.api.GoogleHotelsApi;
import com.packt.spring.ai.examples.travel.provider.google.model.FlightSearchQuery;
import com.packt.spring.ai.examples.travel.provider.google.model.HotelSearchQuery;

import io.codeprimate.extensions.spring.core.http.ClientHttpResponseWrapper;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootConfiguration} for {@literal SerpApi}.
 *
 * @author John Blum
 * @see SpringBootConfiguration
 * @see SerpApiProperties
 * @since 0.1.0
 */
@Slf4j
@SpringBootConfiguration
@EnableConfigurationProperties(SerpApiProperties.class)
public class SerpApiConfiguration {

	@Bean
	public GoogleFlightsApi googleFlightsApi(SerpApiProperties properties) {

		RestClient restClient = buildRestClient(properties, properties.getEngine()::getHotels);

		RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);

		HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter)
			.customArgumentResolver(new FlightSearchQuery.FlightSearchQueryArgumentResolver(properties))
			.build();

		return proxyFactory.createClient(GoogleFlightsApi.class);
	}

	@Bean
	public GoogleHotelsApi googleHotelsApi(SerpApiProperties properties) {

		RestClient restClient = buildRestClient(properties, properties.getEngine()::getFlights);

		RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);

		HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter)
			.customArgumentResolver(new HotelSearchQuery.HotelSearchQueryArgumentResolver(properties))
			.build();

		return proxyFactory.createClient(GoogleHotelsApi.class);
	}

	private RestClient buildRestClient(SerpApiProperties properties, Supplier<String> searchEngineSupplier) {

		return RestClient.builder()
			.baseUrl(properties.getBaseUri())
			.defaultHeaders(consumeHttpHeaders(properties))
			.requestInterceptor(loggingClientHttpRequestInterceptor(properties))
			.build();
	}

	private Consumer<HttpHeaders> consumeHttpHeaders(SerpApiProperties properties) {
		return httpHeaders -> httpHeaders.setBearerAuth(properties.getApiKey());
	}

	@SuppressWarnings("unused")
	private ClientHttpRequestInterceptor loggingClientHttpRequestInterceptor(SerpApiProperties properties) {

		return (request, body, execution) -> {

			log.info("HTTP URL [{}]", request.getURI());
			log.info("HTTP REQUEST BODY [{}]", new String(body));
			log.info("HTTP REQUEST HEADERS [{}]", request.getHeaders());

			ClientHttpResponse response = execution.execute(request, body);

			if (log.isDebugEnabled()) {
				response = ClientHttpResponseWrapper.wrap(response);
				log.debug("HTTP RESPONSE BODY [{}]", response);
			}

			return response;
		};
	}
}
