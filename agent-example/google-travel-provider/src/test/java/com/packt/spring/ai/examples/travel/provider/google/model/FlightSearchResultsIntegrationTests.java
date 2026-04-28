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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

/**
 * Unit Tests for {@link FlightSearchResults},
 *
 * @author John Blum
 * @see SpringBootTest
 * @see FlightSearchResults
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
@SpringBootTest
public class FlightSearchResultsIntegrationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@Value("classpath:google-flights-search-results.json")
	private Resource json;

	@Test
	void parsesGoogleFlightsSearchResultsJson() throws IOException {

		FlightSearchResults searchResults =
			this.objectMapper.readValue(json.getInputStream(), FlightSearchResults.class);

		assertThat(searchResults).isNotNull();
		assertThat(searchResults).hasSizeGreaterThan(0);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfiguration {

	}
}
