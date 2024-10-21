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
package io.codeprimate.tools.spring.ai.tokens.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.codeprimate.tools.spring.ai.tokens.TokenMetadataApplication;

import org.junit.jupiter.api.Test;

import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;

/**
 * Integration Tests for {@link TokenCountService}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest(classes = TokenMetadataApplication.class)
@ActiveProfiles(TokenMetadataApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class TokenCountServiceIntegrationTests {

	@Value("classpath:/static/img/SpringLogo.png")
	private Resource springLogo;

	@Autowired
	private TokenCountService tokenCountService;

	@Test
	void countTokenFromText() {
		assertThat(this.tokenCountService.countTokens("This is a test")).isEqualTo(4);
	}

	@Test
	void countTokenFromTextWithPunctuation() {
		assertThat(this.tokenCountService.countTokens("This is another test!")).isEqualTo(5);
	}

	@Test
	void countTokensFromMedia() {

		Media media = new Media(MimeType.valueOf("image/png"), this.springLogo);
		int tokenCount = this.tokenCountService.countTokens(media);

		assertThat(tokenCount).isGreaterThan(0);
	}
}
