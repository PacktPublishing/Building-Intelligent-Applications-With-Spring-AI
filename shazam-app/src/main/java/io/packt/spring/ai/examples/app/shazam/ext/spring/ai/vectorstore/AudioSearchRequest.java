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
package io.packt.spring.ai.examples.app.shazam.ext.spring.ai.vectorstore;

import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioUtils;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;

import org.cp.elements.lang.Assert;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link MediaSearchRequest} implementation for {@link Audio}.
 *
 * @author John Blum
 * @see Audio
 * @see MediaSearchRequest
 * @see org.springframework.ai.vectorstore.SearchRequest
 * @since 0.1.0
 */
@Getter
public class AudioSearchRequest extends MediaSearchRequest {

	public static AudioBuilder builder(SearchRequest searchRequest) {
		return new AudioSearchRequestBuilder(searchRequest);
	}

	private final Audio audio;

	protected AudioSearchRequest(SearchRequest searchRequest, Audio audio) {
		super(searchRequest, AudioUtils.assertAudio(audio).getMedia());
		this.audio = audio;
	}

	@Override
	public Document toDocument() {
		return AbstractDocumentStore.newAudioDocument(getAudio(), getId());
	}

	public interface AudioBuilder extends MediaBuilder {
		org.cp.elements.lang.Builder<AudioSearchRequest> query(Audio audio);
	}

	@Getter(AccessLevel.PROTECTED)
	protected static class AudioSearchRequestBuilder implements AudioBuilder,
			org.cp.elements.lang.Builder<AudioSearchRequest> {

		private Audio audio;

		private final SearchRequest searchRequest;

		protected AudioSearchRequestBuilder(SearchRequest searchRequest) {
			Assert.notNull(searchRequest, "SearchRequest is required");
			this.searchRequest = searchRequest;
		}

		public org.cp.elements.lang.Builder<MediaSearchRequest> query(Media media) {
			this.audio = Audio.from(media);
			return null;
		}

		@Override
		public AudioSearchRequestBuilder query(Audio audio) {
			this.audio = AudioUtils.assertAudio(audio);
			return this;
		}

		@Override
		public AudioSearchRequest build() {
			return new AudioSearchRequest(getSearchRequest(), getAudio());
		}
	}
}
