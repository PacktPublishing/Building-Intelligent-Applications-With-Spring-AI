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
package io.packt.spring.ai.examples.app.shazam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Shazam's song search.
 *
 * @author John Blum
 * @see ConfigurationProperties
 * @since 0.1.0
 */
@Data
@ConfigurationProperties(prefix = "shazam.song.search")
public class SongSearchProperties {

	protected static final double DEFAULT_SIMILARITY_THRESHOLD = 0.75d;
	protected static final int DEFAULT_TOP_K = 8;

	private Double similarityThreshold;

	private Integer topK;

	public double resolveSimilarityThreshold() {
		Double similarityThreshold = getSimilarityThreshold();
		return similarityThreshold != null ? similarityThreshold : DEFAULT_SIMILARITY_THRESHOLD;
	}

	public int resolveTopK() {
		Integer topK = getTopK();
		return topK != null ? topK : DEFAULT_TOP_K;
	}
}
