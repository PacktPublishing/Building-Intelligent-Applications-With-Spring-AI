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

import io.packt.spring.ai.examples.app.shazam.SongLoader;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Spring {@link ConfigurationProperties} for {@link SongLoader}.
 *
 * @author John Blum
 * @see SongLoader
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 0.1.0
 */
@Data
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "shazam.song.loader")
public class SongLoaderProperties {

	private AudioClip audioClip = new AudioClip();

	private Song song = new Song();

	public boolean isAudioClipSavingEanbled() {
		return getAudioClip().isSaveEnabled();
	}

	public boolean isSongLoadingEnabled() {
		return getSong().isLoadEnabled();
	}

	@Data
	public static class AudioClip {

		private Boolean save;

		public boolean isSaveEnabled() {
			return Boolean.TRUE.equals(getSave());
		}
	}

	@Data
	public static class Song {

		private Boolean load;

		public boolean isLoadEnabled() {
			return Boolean.TRUE.equals(getLoad());
		}
	}
}
