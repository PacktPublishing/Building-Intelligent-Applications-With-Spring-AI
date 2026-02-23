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
package io.packt.spring.ai.examples.app.shazam.util;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;

import org.cp.elements.lang.Assert;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.lang.NonNull;

/**
 * Abstract utility functions for Spring AI {@link Document Documents}.
 *
 * @author John Blum
 * @see Document
 * @since 0.1.0
 */
public class DocumentUtils {

	public static @NonNull Document assertDocument(Document document) {
		Assert.notNull(document, "Document is required");
		return document;
	}

	public static @NonNull Media asssertMedia(Media media) {
		Assert.notNull(media, "Media is required");
		return media;
	}

	public static Audio toAudio(Document document) {

		assertDocument(document);

		if (document instanceof AbstractDocumentStore.AudioDocument audioDocument) {
			return audioDocument.getAudio();
		}
		else {
			Media media = asssertMedia(document.getMedia());
			byte[] data = media.getDataAsByteArray();
			return Audio.from(data);
		}
	}
}
