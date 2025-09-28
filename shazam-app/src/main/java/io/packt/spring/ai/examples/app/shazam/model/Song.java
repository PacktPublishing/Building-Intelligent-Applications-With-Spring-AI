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
package io.packt.spring.ai.examples.app.shazam.model;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a {@literal song}.
 *
 * @author John Blum
 * @see Audio
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
@ToString(of = { "artist", "album", "title" })
@EqualsAndHashCode(of = { "artist", "album", "title" })
public class Song {

	private static final String NO_ALBUM = null;

	public static Song.Builder builder() {
		return new Song.Builder();
	}

	private Audio audio;

	private final String artist;
	private final String album;
	private final String title;

	private final UUID id;

	private Song(String artist, String title) {
		this(artist, NO_ALBUM, title);
	}

	private Song(String artist, String album, String title) {

		Assert.hasText(artist, "Artist of song is required");
		Assert.hasText(title, "Song title is required");

		this.id = UUID.randomUUID();
		this.artist = artist;
		this.album = album;
		this.title = title;
	}

	public Song with(Audio audio) {
		this.audio = Audio.nullSafe(audio);
		return this;
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private Audio audio;

		private String artist;
		private String album;
		private String title;

		public Builder by(String artist) {
			Assert.hasText(artist, "Artist of song is required");
			this.artist = artist;
			return this;
		}

		public Builder from(String album) {
			Assert.hasText(album, "Album of song is required");
			this.album = album;
			return this;
		}

		public Builder with(String title) {
			Assert.hasText(title, "Song title is required");
			this.title = title;
			return this;
		}

		public Builder having(byte[] audio) {
			return having(Audio.from(audio));
		}

		public Builder having(Audio audio) {
			this.audio = audio;
			return this;
		}

		public Builder having(MultipartFile file) {
			return having(Audio.from(file));
		}

		public Builder having(Resource resource) {
			return having(Audio.from(resource));
		}

		public Song build() {
			return new Song(getArtist(), getAlbum(), getTitle()).with(getAudio());
		}
	}
}
