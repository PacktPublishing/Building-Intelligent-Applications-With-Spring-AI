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
package com.packt.spring.ai.examples.travel.provider.google.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import tools.jackson.databind.ObjectMapper;

/**
 * Integration Tests for {@link HotelSearchResults}.
 *
 * @author John Blum
 * @see SpringBootTest
 * @see HotelSearchResults
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
@SpringBootTest
class HotelSearchResultsIntegrationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@Value("classpath:google-hotels-search-results.json")
	private Resource json;

	@Test
	void parsesGoogleHotelsSearchResultsJson() throws IOException {

		HotelSearchResults results =
			this.objectMapper.readValue(json.getInputStream(), HotelSearchResults.class);

		assertThat(results).isNotNull();
		assertThat(results).hasSizeGreaterThan(0);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfiguration {

	}
}
