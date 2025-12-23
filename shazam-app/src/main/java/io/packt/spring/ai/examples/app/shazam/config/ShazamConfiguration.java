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

import io.codeprimate.extensions.spring.boot.web.contoller.AdminController;
import io.packt.spring.ai.examples.app.shazam.ext.spring.ai.embedding.AudioEmbeddingModel;
import io.packt.spring.ai.examples.app.shazam.model.Song;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link SpringBootConfiguration} for the Shazam application.
 *
 * @author John Blum
 * @see SpringBootConfiguration
 * @see EnableConfigurationProperties
 * @see EntityScan
 * @see SongSearchProperties
 * @see AudioProperties
 * @since 0.1.0
 */
@SpringBootConfiguration
@EntityScan(basePackageClasses = Song.class)
@EnableConfigurationProperties({ AudioProperties.class, SongSearchProperties.class })
@SuppressWarnings("unused")
public class ShazamConfiguration {

	@Bean
	AdminController adminController() {
		return new AdminController();
	}

	@Bean
	EmbeddingModel embeddingModel() {
		return new AudioEmbeddingModel();
	}
}
