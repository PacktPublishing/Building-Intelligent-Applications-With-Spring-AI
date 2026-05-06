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
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.Collections;
import java.util.function.Function;

import javax.sound.sampled.AudioFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.spring.core.io.ResourceUtils;
import io.codeprimate.extensions.util.ExceptionThrowingRunnable;
import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.config.SongLoaderProperties;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.tritonus.MpegAudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;
import io.packt.spring.ai.examples.app.shazam.service.SongProcessor;
import io.packt.spring.ai.examples.app.shazam.support.SongLoadException;
import io.packt.spring.ai.examples.app.shazam.util.NumberUtils;

import org.cp.elements.io.FileUtils;
import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.cp.elements.util.ArrayUtils;
import org.springframework.ai.document.Document;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link SpringBootApplication} used to load {@link Song Songs} for the {@link ShazamApplication}.
 *
 * @author John Blum
 * @see SpringBootApplication
 * @see AbstractSpringBootApplication
 * @see ApplicationRunner
 * @see MusicService
 * @see Song
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
		runSpringApplication(SongLoader.class, asStringArray(SONG_LOADER_PROFILE), applicationBuilderFunction(), args);
	}

	private static Function<SpringApplicationBuilder, SpringApplicationBuilder> applicationBuilderFunction() {
		return springApplicationBuilder ->
			springApplicationBuilder.properties(Collections.singletonMap("spring.docker.compose.enabled", "false"));
	}

	@SpringBootConfiguration
	@Profile(SONG_LOADER_PROFILE)
	@EnableConfigurationProperties(SongLoaderProperties.class)
	static class SongLoaderConfiguration {

		@Bean
		SongLoaderContext songLoaderContext(
			MusicService musicService,
			ObjectMapper objectMapper,
			ResourceLoader resourceLoader,
			SongLoaderProperties songLoaderProperties
		) {
			return SongLoaderContext.from(musicService, objectMapper, resourceLoader, songLoaderProperties);
		}

		@Bean
		SongLoaderService songLoaderService(SongLoaderContext songLoaderContext) {
			return SongLoaderService.from(songLoaderContext);
		}
	}

	@Bean
	ApplicationRunner programRunner(SongLoaderContext songLoaderContext, SongLoaderService songLoaderService) {

		return args -> {

			log.info("Shazam Song Loader");

			Resource resourceLocation = ResourceUtils.newResource(SONG_DATABASE_DATA_LOCATION);

			File directory = resourceLocation.getFile();

			File[] songMetadataFiles = ArrayUtils.nullSafeArray(directory.listFiles(songMetadataFileFilter()));

			for (File songMetadataFile : songMetadataFiles) {

				log.info("Found song metadata [{}]", songMetadataFile);

				SongMetadata songMetadata = songLoaderService.loadSongMetadata(songMetadataFile);
				Song song = songLoaderService.loadSong(songMetadata);

				log.info("Loading song [{}] by artist [{}]...", song.getTitle(), song.getArtist());

				ExceptionThrowingRunnable.runSafely(
					() -> songLoaderService.store(song, randomAudioClipWriter(songLoaderContext)),
					cause -> logWarn(cause.getMessage())
				);
			}
		};
	}

	@SuppressWarnings("all")
	private SongProcessor randomAudioClipWriter(SongLoaderContext songLoaderContext) {

		return (song, audioDocuments) -> {

			if (songLoaderContext.isSaveAudioClip()) {

				int index = NumberUtils.randomInt(audioDocuments.size());

				Document audioClipDocument = audioDocuments.get(index);

				File audioFile = song.getAudio().file();

				String audioFilename = FileUtils.getName(audioFile);
				String audioFileExtension = FileUtils.getExtension(audioFile);
				String audioClipFilename = "%s-clip.%s".formatted(audioFilename, audioFileExtension);

				File audioClipFile = new File(audioFile.getParentFile(), audioClipFilename);

				if (!audioClipFile.isFile()) {

					byte[] audioData = audioClipDocument.getMedia().getDataAsByteArray();

					ExceptionThrowingRunnable.runSafely(() -> saveToFile(audioClipFile, audioData), cause -> {
						log.error("Failed to write audio data for song [{}}] to file [{}]",
							audioFilename, audioClipFile.getAbsolutePath());
						log.debug("Caused by: ", cause);
					});
				}
			}

			if (songLoaderContext.isNotLoadSong()) {
				String message = "Loading Song [%s] has been short-circuited".formatted(song);
				throw SongLoadException.because(message);
			}

			return audioDocuments;
		};
	}

	private void saveToFile(File audioClipFile, byte[] audioData) throws IOException {

		log.info("Saving audio clip to file [{}}]", audioClipFile.getAbsolutePath());

		try (FileOutputStream audioFileOutputStream = new FileOutputStream(audioClipFile, false)) {
			audioFileOutputStream.write(audioData);
			audioFileOutputStream.flush();
		}
	}

	private FileFilter songMetadataFileFilter() {
		return file -> file != null && file.isFile() && file.getName().endsWith("metadata.json");
	}

	@Getter(AccessLevel.PACKAGE)
	static class AudioBuilder implements Builder<Audio> {

		static AudioBuilder from(Resource audioResource) {
			return new AudioBuilder(audioResource);
		}

		private final Audio audio;

		AudioBuilder(Resource resource) {
			Assert.notNull(resource, "Resource containing audio is required");
			this.audio = Audio.from(resource);
		}

		@Override
		public Audio build() {
			Audio audio = getAudio();
			return audio.in(resolveAudioFormat(audio));
		}

		private AudioFormat resolveAudioFormat(Audio audio) {
			return ExceptionThrowingSupplier.getSafely(AudioFormatBuilder.from(audio)::build, cause ->
				buildAudioFormat(audio));
		}

		private AudioFormat buildAudioFormat(Audio audio) {
			return MpegAudioFormatBuilder.mpegOneLayerThree(audio)
				.withSampleRateOf44100()
				.build();
		}
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

		private File source;

		private Year year;

		public SongMetadata from(File songMetadataSource) {
			this.source = songMetadataSource;
			return this;
		}

		public Song toSong(ResourceLoader resourceLoader) {

			Resource audioResource = resourceLoader.getResource(getResourcePath());

			Audio audio = AudioBuilder.from(audioResource).build();

			return Song.builder()
				.by(getArtist())
				.from(getAlbum())
				.with(getTitle())
				.having(audio)
				.build();
		}
	}

	interface SongLoaderContext {

		static SongLoaderContext from(
			MusicService musicService,
			ObjectMapper objectMapper,
			ResourceLoader resourceLoader,
			SongLoaderProperties songLoaderProperties
		) {

			Assert.notNull(musicService, "MusicService is required");
			Assert.notNull(objectMapper, "JSON ObjectMapper is required");
			Assert.notNull(resourceLoader, "ResourceLoader is required");
			Assert.notNull(songLoaderProperties, "SongLoaderProperties are required");

			return new SongLoaderContext() {

				@Override
				public MusicService getMusicService() {
					return musicService;
				}

				@Override
				public ObjectMapper getObjectMapper() {
					return objectMapper;
				}

				@Override
				public ResourceLoader getResourceLoader() {
					return resourceLoader;
				}

				@Override
				public SongLoaderProperties getSongLoaderProperties() {
					return songLoaderProperties;
				}
			};
		}

		MusicService getMusicService();

		ObjectMapper getObjectMapper();

		ResourceLoader getResourceLoader();

		SongLoaderProperties getSongLoaderProperties();

		default boolean isSaveAudioClip() {
			return getSongLoaderProperties().isAudioClipSavingEnabled();
		}

		default boolean isNotLoadSong() {
			return getSongLoaderProperties().isSongLoadingDisabled();
		}
	}

	interface SongLoaderService {

		static SongLoaderService from(SongLoaderContext context) {
			Assert.notNull(context, "SongLoaderContext is required");
			return () -> context;
		}

		SongLoaderContext getContext();

		default MusicService getMusicService() {
			return getContext().getMusicService();
		}

		default ResourceLoader getResourceLoader() {
			return getContext().getResourceLoader();
		}

		default Song loadSong(SongMetadata songMetadata) {
			return songMetadata.toSong(getResourceLoader());
		}

		default SongMetadata loadSongMetadata(File songMetadataFile) {

			ObjectMapper objectMapper = getContext().getObjectMapper();

			SongMetadata songMetadata = ExceptionThrowingSupplier.getSafely(
				() -> objectMapper.readValue(songMetadataFile, SongMetadata.class),
				cause -> {
					throw SongLoadException.from(songMetadataFile, cause);
				}
			);

			songMetadata.from(songMetadataFile);

			return songMetadata;
		}

		default void store(Song song) {
			getMusicService().store(song);
		}

		default void store(Song song, SongProcessor songProcessor) {
			getMusicService().store(song, songProcessor);
		}
	}
}
