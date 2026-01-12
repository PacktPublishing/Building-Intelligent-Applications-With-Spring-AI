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

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link SearchRequest} implementation for Spring AI {@link Media}.
 *
 * @author John Blum
 * @see SearchRequest
 * @see Document
 * @see Media
 * @since 0.1.0
 */
@Getter
public class MediaSearchRequest extends SearchRequest {

	public static MediaBuilder builder(SearchRequest searchRequest) {
		return new MediaSearchRequestBuilder(searchRequest);
	}

	private final Media media;

	protected MediaSearchRequest(SearchRequest searchRequest, Media media) {
		super(searchRequest);
		this.media = assertMedia(media);
	}

	private Media assertMedia(Media media) {
		Assert.notNull(media, "Media is required");
		return media;
	}

	@Override
	@SuppressWarnings("all")
	public Filter.Expression getFilterExpression() {
		return null;
	}

	protected String getId() {
		return getQuery();
	}

	public Document toDocument() {
		return Document.builder()
			.media(getMedia())
			.id(getId())
			.build();
	}

	public interface MediaBuilder {
		org.cp.elements.lang.Builder<MediaSearchRequest> query(Media media);
	}

	@Getter(AccessLevel.PROTECTED)
	protected static class MediaSearchRequestBuilder implements MediaBuilder,
			org.cp.elements.lang.Builder<MediaSearchRequest> {

		private Media media;

		private final SearchRequest searchRequest;

		protected MediaSearchRequestBuilder(SearchRequest searchRequest) {
			Assert.notNull(searchRequest, "SearchRequest is required");
			this.searchRequest = searchRequest;
		}

		@Override
		public MediaSearchRequestBuilder query(Media media) {
			Assert.notNull(media, "Media is required");
			this.media = media;
			return this;
		}

		@Override
		public MediaSearchRequest build() {
			return new MediaSearchRequest(getSearchRequest(), getMedia());
		}
	}
}
