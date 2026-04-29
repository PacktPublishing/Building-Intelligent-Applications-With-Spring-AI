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
package io.packt.spring.ai.examples.app.shazam.service.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.UUID;

import io.codeprimate.extensions.data.caching.SimpleCache;
import io.packt.spring.ai.examples.app.shazam.config.ShazamConfiguration;
import io.packt.spring.ai.examples.app.shazam.config.SongSearchProperties;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.repo.DocumentStore;
import io.packt.spring.ai.examples.app.shazam.repo.SongRepository;
import io.packt.spring.ai.examples.app.shazam.service.AudioSplitter;
import io.packt.spring.ai.examples.app.shazam.support.SongNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tests for {@link SmartMusicService}.
 *
 * @author John Blum
 * @see SmartMusicService
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @see org.springframework.boot.test.context.SpringBootTest
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles({ "honerlaw" })
@Disabled("Shazam app is incomplete")
@EnabledIf("resourceExists")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("unused")
public class SmartMusicServiceIntegrationTests {

	private static final String NIRVANA_NEVERMIND_COME_AS_YOU_ARE_AUDIO_CLIP_RESOURCE_PATH = "Nirvana-Nevermind-ComeAsYouAre-clip-1s.wav";
	private static final String PEARL_JAM_NO_CODE_JEREMY_AUDIO_CLIP_RESOURCE_PATH = "PearlJam-Ten-Jeremy-clip-1s.wav";
	private static final String PEARL_JAM_NO_CODE_JEREMY_SONG_RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	static Resource nirvanaNevermindComeAsYouAreAudioClipResource() {
		return new ClassPathResource(NIRVANA_NEVERMIND_COME_AS_YOU_ARE_AUDIO_CLIP_RESOURCE_PATH);
	}

	static Resource pearlJamTenJeremyAudioClipResource() {
		return new ClassPathResource(PEARL_JAM_NO_CODE_JEREMY_AUDIO_CLIP_RESOURCE_PATH);
	}

	static Resource pearlJamTenJeremySongResource() {
		return new ClassPathResource(PEARL_JAM_NO_CODE_JEREMY_SONG_RESOURCE_PATH);
	}

	static boolean resourceExists() {
		return pearlJamTenJeremySongResource().exists() && pearlJamTenJeremyAudioClipResource().exists();
	}

	@Autowired
	private SmartMusicService musicService;

	@Autowired
	private SongRepository songRepository;

	@Test
	@Order(1)
	void store() {

 		Audio audio = Audio.from(pearlJamTenJeremySongResource());

		Song song = Song.builder()
			.by("Pearl Jam")
			.from("Ten")
			.with("Jeremy")
			.having(audio)
			.build();

		this.musicService.store(song);

		assertThat(this.songRepository.count()).isOne();
	}

	@Test
	@Order(2)
	void search() {

		Audio audioClip = Audio.from(pearlJamTenJeremyAudioClipResource());
		Song song = this.musicService.search(audioClip);

		assertThat(song).isNotNull();
		assertThat(song.getArtist()).isEqualTo("Pearl Jam");
		assertThat(song.getAlbum()).isEqualTo("Ten");
		assertThat(song.getTitle()).isEqualTo("Jeremy");
	}

	@Test
	@Order(3)
	void searchDoesNotMatchSong() {

		Audio audioClip = Audio.from(nirvanaNevermindComeAsYouAreAudioClipResource());

		assertThatExceptionOfType(SongNotFoundException.class)
			.isThrownBy(() -> {
				Song song = this.musicService.search(audioClip);
				System.err.printf("Expected SongNotFoundException but found Song [%s]%n", song);
			})
			.withNoCause();
	}

	@SpringBootConfiguration
	@Import(ShazamConfiguration.class)
	@EnableAutoConfiguration(exclude = PgVectorStoreAutoConfiguration.class)
	static class TestConfiguration {

		@Bean
		SmartMusicService smartMusicService(
			AudioSplitter audioSplitter,
			DocumentStore documentStore,
			SongRepository songRepository,
			SongSearchProperties songSearchProperties,
			VectorStore vectorStore
		) {
			return new SmartMusicService(audioSplitter, documentStore, songRepository, songSearchProperties, vectorStore);
		}

		@Bean
		SongRepository songRepository() {

			SimpleCache<UUID, Song> songCache = SimpleCache.inMemory();
			SongRepository songRepository = mock(SongRepository.class);

			doAnswer(answer -> {
				UUID id = answer.getArgument(0);
				return Optional.ofNullable(songCache.get(id));
			}).when(songRepository).findById(isA(UUID.class));

			doAnswer(answer -> {
				Song song = answer.getArgument(0);
				songCache.put(song.getId(), song);
				return song;
			}).when(songRepository).save(isA(Song.class));

			doAnswer(answer -> songCache.size()).when(songRepository).count();

			return songRepository;
		}

		@Bean
		VectorStore vectorStore(EmbeddingModel embeddingModel) {
			return SimpleVectorStore.builder(embeddingModel).build();
		}
	}
}
