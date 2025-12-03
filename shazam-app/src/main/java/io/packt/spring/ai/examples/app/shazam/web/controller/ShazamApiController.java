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
package io.packt.spring.ai.examples.app.shazam.web.controller;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.Song;
import io.packt.spring.ai.examples.app.shazam.service.MusicService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Web MVC {@link RestController} used to process main functions of the Shazam application.
 *
 * @author John Blum
 * @see RestController
 * @see RequestMapping
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ShazamApiController {

	private final MusicService musicService;

	@PostMapping("/song/search")
	public SongSearchResponse search(@RequestBody MultipartFile file) {
		Audio audio = Audio.from(file);
		Song song = getMusicService().search(audio);
		return SongSearchResponse.from(song);
	}

	@PostMapping("/songs")
	public ResponseEntity<Object> upload(@ModelAttribute UploadSongForm form) {
		Song song = form.toSong();
		getMusicService().store(song);
		return ResponseEntity.ok().build();
	}

	public interface SongSearchResponse {

		static SongSearchResponse from(Song song) {

			return new SongSearchResponse() {

				@Override
				public String getArtist() {
					return song.getArtist();
				}

				@Override
				public String getTitle() {
					return song.getTitle();
				}
			};
		}

		String getArtist();

		String getTitle();

	}

	@Data
	public static class UploadSongForm {

		private String artist;
		private String album;
		private String songTitle;

		private MultipartFile songFile;

		public Song toSong() {

			return Song.builder()
				.by(getArtist())
				.from(getAlbum())
				.with(getSongTitle())
				.having(getSongFile())
				.build();
		}
	}
}
