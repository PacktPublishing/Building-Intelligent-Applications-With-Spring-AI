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
package io.packt.spring.ai.examples.app.shazam;

import java.io.File;
import java.io.FileFilter;
import java.time.Year;
import java.util.Collections;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} used to load {@link Song Songs} for the {@link ShazamApplication}.
 *
 * @author John Blum
 * @see SpringBootApplication
 * @see AbstractSpringBootApplication
 * @since 0.1.0
 */
@Slf4j
@SpringBootApplication
@SuppressWarnings("unused")
@Profile(SongLoader.SONG_LOADER_PROFILE)
public class SongLoader extends AbstractSpringBootApplication {

	public static final String SONG_LOADER_PROFILE = "shazam-song-loader";

	private static final String SONG_DATABASE_DATA_LOCATION = "/database/data";

	public static void main(String[] args) {

		Function<SpringApplicationBuilder, SpringApplicationBuilder> applicationBuilder = springApplicationBuilder ->
			springApplicationBuilder.properties(Collections.singletonMap("spring.docker.compose.enabled", "false"));

		runSpringApplication(SongLoader.class, asStringArray(SONG_LOADER_PROFILE), applicationBuilder, args);
	}

	@Bean
	ApplicationRunner programRunner(MusicService musicService, ObjectMapper objectMapper, ResourceLoader resourceLoader) {

		return args -> {

			log.info("Shazam Song Loader");

			Resource resource = new ClassPathResource(SONG_DATABASE_DATA_LOCATION);

			Assert.state(resource.exists(), () -> "Resource location [%s] for song data not found"
				.formatted(SONG_DATABASE_DATA_LOCATION));

			File directory = resource.getFile();
			File[] songMetadataFiles = nullSafeFileArray(directory.listFiles(songMetadataFileFilter()));

			for (File songMetadataFile : songMetadataFiles) {

				log.info("Found song metadata [{}]", songMetadataFile);

				SongMetadata songMetadata = objectMapper.readValue(songMetadataFile, SongMetadata.class);
				Song song = songMetadata.toSong(resourceLoader);

				log.info("Loading song [{}] by artist [{}]...", song.getTitle(), song.getArtist());

				musicService.store(song);
			}
		};
	}

	private File[] nullSafeFileArray(File[] array) {
		return array != null ? array : new File[0];
	}

	private FileFilter songMetadataFileFilter() {
		return file -> file != null && file.isFile() && file.getName().endsWith("metadata.json");
	}

	@Getter
	@ToString
	@EqualsAndHashCode
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class SongMetadata {

		private String artist;
		private String album;
		private String resourcePath;
		private String title;
		private Year year;

		public Song toSong(ResourceLoader resourceLoader) {

			Resource audioResource = resourceLoader.getResource(getResourcePath());

			return Song.builder()
				.by(getArtist())
				.from(getAlbum())
				.with(getTitle())
				.having(audioResource)
				.build();
		}
	}
}
