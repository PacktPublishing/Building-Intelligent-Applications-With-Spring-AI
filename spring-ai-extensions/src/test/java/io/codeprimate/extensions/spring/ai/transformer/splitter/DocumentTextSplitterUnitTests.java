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
package io.codeprimate.extensions.spring.ai.transformer.splitter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import lombok.Getter;
import tools.jackson.databind.ObjectMapper;

/**
 * Unit Tests for {@link DocumentTextSplitter}
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.autoconfigure.json.JsonTest
 * @see io.codeprimate.extensions.spring.ai.transformer.splitter.DocumentTextSplitter
 * @since 0.1.0
 */
@JsonTest
@ActiveProfiles("test")
@SuppressWarnings("unused")
public class DocumentTextSplitterUnitTests {

	@Autowired
	private ObjectMapper objectMapper;

	private final DocumentTextSplitter textSplitter = new DocumentTextSplitter();

	@Test
	void splitsTextCorrectly() throws IOException {

		Resource pearlJamBlack = new ClassPathResource("ledzeppelin-stairwaytoheaven.json");

		Song song = this.objectMapper.readValue(pearlJamBlack.getContentAsByteArray(), Song.class);

		Assertions.assertThat(song).isNotNull();
		Assertions.assertThat(song.getArtist()).isEqualTo("Led Zeppelin");
		Assertions.assertThat(song.getTitle()).isEqualTo("Stairway to Heaven");

		String lyrics = song.getLyrics();

		assertThat(lyrics).isNotBlank();

		List<String> splitLyrics = this.textSplitter.splitText(lyrics);

		assertThat(splitLyrics).isNotNull();
		assertThat(splitLyrics).hasSize(1);

		String expectedLyrics = preProcess(lyrics);
		String actualLyrics = splitLyrics.get(0);

		assertThat(actualLyrics).isEqualTo(expectedLyrics);
	}

	private String preProcess(String text) {
		return this.textSplitter.preProcess(text);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class DocumentTextSplitterConfiguration {

	}

	@Getter
	static class Song {

		private String artist;
		private String lyrics;
		private String title;

	}
}
