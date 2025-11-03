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

import org.cp.elements.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a {@literal song}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSource
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 * @since 0.1.0
 */
@Entity
@Getter
@Table(name = "Songs")
@ToString(of = { "artist", "album", "title" })
@EqualsAndHashCode(of = { "artist", "album", "title" })
@SuppressWarnings("unused")
public class Song implements AudioSource {

	private static final String NO_ALBUM = null;

	/**
	 * Factory method returning a {@link Song.Builder} used to construct a new {@link Song} using a DSL.
	 *
	 * @return a new {@link Song.Builder} used to construct a new {@link Song}.
	 */
	public static Song.Builder builder() {
		return new Song.Builder();
	}

	@Transient
	private transient Audio audio;

	private final String artist;
	private final String album;
	private final String title;

	@Id
	@SuppressWarnings("all")
	private UUID id;

	/**
	 * Constructs a new {@link Song} with the given {@link String artist} and {@link String title}.
	 *
	 * @param artist {@link String} containing the name of the song's artist.
	 * @param title {@link String} containing the title of the song.
	 * @throws IllegalArgumentException if {@link String artist} or {@link String title}
	 * are {@literal null} or blank.
	 */
	private Song(String artist, String title) {
		this(artist, NO_ALBUM, title);
	}

	/**
	 * Constructs a new {@link Song} with the given {@link String artist} and {@link String title}.
	 *
	 * @param artist {@link String} containing the name of the song's artist.
	 * @param album {@link String} containing the name of album from which this song originates.
	 * @param title {@link String} containing the title of the song.
	 * @throws IllegalArgumentException if {@link String artist} or {@link String title}
	 * are {@literal null} or blank.
	 */
	@PersistenceCreator
	Song(String artist, String album, String title) {

		Assert.hasText(artist, "Artist of song is required");
		Assert.hasText(title, "Song title is required");

		this.id = UUID.randomUUID();
		this.artist = artist;
		this.album = album;
		this.title = title;
	}

	@Transient
	public boolean isSingle() {
		return !StringUtils.hasText(getAlbum());
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

		public Builder having(MultipartFile audio) {
			return having(Audio.from(audio));
		}

		public Builder having(Resource audio) {
			return having(Audio.from(audio));
		}

		public Song build() {
			return new Song(getArtist(), getAlbum(), getTitle()).with(getAudio());
		}
	}
}
