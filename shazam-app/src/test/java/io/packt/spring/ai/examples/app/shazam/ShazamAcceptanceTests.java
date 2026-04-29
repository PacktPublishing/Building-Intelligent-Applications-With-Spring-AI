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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.sound.sampled.AudioFormat;

import io.packt.spring.ai.examples.app.shazam.ext.tritonus.MpegAudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;
import io.packt.spring.ai.examples.app.shazam.support.SongNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Acceptance Tests for the Shazam application.
 *
 * @author John Blum
 * @see ShazamApplication
 * @since 0.1.0
 */
@SpringBootTest
@Profile("acceptance-tests")
@Disabled("Shazam app is incomplete")
@SuppressWarnings("unused")
public class ShazamAcceptanceTests {

	@Autowired
	private MusicService musicService;

	@SuppressWarnings("all")
	private void testSearchSuccess(String resourcePath, Consumer<Song> songAssertions) {
		testSearch(resourcePath, songAssertions, Assertions::fail);
	}

	@SuppressWarnings("all")
	private void testSearchFailure(String resourcePath, Function<Exception, ?> exceptionHandler) {
		testSearch(resourcePath, song -> fail("Expected Song Not Found"), exceptionHandler);
	}

	private void testSearch(String resourcePath, Consumer<Song> songAssertions, Function<Exception, ?> exceptionHandler) {

		Resource resource = new ClassPathResource(resourcePath);
		Audio audio = Audio.from(resource);
		AudioFormat audioFormat = MpegAudioFormatBuilder.mpegOneLayerThree(audio).withSampleRateOf44100().build();

		audio = audio.in(audioFormat);

		try {
			Song song = this.musicService.search(audio);
			songAssertions.accept(song);
		}
		catch (Exception cause) {
			exceptionHandler.apply(cause);
		}
	}

	@Test
	void sameStudioPealJamSongIsMatch() {

		testSearchSuccess("PearlJam-NoCode-RedMosquito-clip.mp3", song -> {
			assertThat(song).isNotNull();
			assertThat(song.getArtist()).isEqualTo("Pearl Jam");
			assertThat(song.getTitle()).isEqualTo("Red Mosquito");
		});
	}

	// For example, artist's live (concert) version of song
	@Test
	void similarLivePearlJamSongIsMatch() {

		testSearchSuccess("PearlJam-RedMosquito-Live-clip.mp3", song -> {
			assertThat(song).isNotNull();
			assertThat(song.getArtist()).isEqualTo("Pearl Jam");
			assertThat(song.getTitle()).isEqualTo("Red Mosquito");
		});
	}

	@Test
	void sameArtistDifferentSongDoesNotMatch() {

		testSearchFailure("PearlJam-Vs-GlorifiedG-clip.mp3", cause ->
			assertThat(cause).isInstanceOf(SongNotFoundException.class));
	}

	@Test
	void differentArtistDifferentSongDoesNotMatch() {
		fail("Not Implemented");
	}

	@Test
	void differentArtistSameSongDoesNotMatch() {
		fail("Not Implemented");
	}

	@Test
	void matchbox20SongIsMatch() {

		testSearchSuccess("Matchbox20-Unwell-clip.mp3", song -> {
			assertThat(song).isNotNull();
			assertThat(song.getArtist()).isEqualTo("Matchbox20");
			assertThat(song.getTitle()).isEqualTo("Unwell");
		});
	}
}
