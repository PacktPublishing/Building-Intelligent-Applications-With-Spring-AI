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

import java.util.Set;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling audio data.
 *
 * @author John Blum
 * @see org.springframework.ai.document.Document
 * @see org.springframework.core.io.Resource
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class Audio {

	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	protected static final MimeType AUDIO_MP3 = MimeType.valueOf("audio/mpeg");

	public static Audio empty() {
		return from(EMPTY_BYTE_ARRAY);
	}

	public static Audio from(byte[] data) {
		return new Audio(data);
	}

	public static Audio from(MultipartFile file) {
		Assert.notNull(file, "File is required");
		return from(ExceptionThrowingSupplier.getSafely(file::getBytes));
	}

	public static Audio from(Resource resource) {
		Assert.notNull(resource, "Resource is required");
		return from(ExceptionThrowingSupplier.getSafely(resource::getContentAsByteArray));
	}

	public static Audio nullSafe(Audio audio) {
		return audio != null ? audio : empty();
	}

	private final byte[] data;

	private Format format;

	public Audio(byte[] data) {
		this.data = data != null ? data : EMPTY_BYTE_ARRAY;
	}

	public Resource getResource() {
		return new ByteArrayResource(getData());
	}

	public Audio in(Format format) {
		this.format = format;
		return this;
	}

	public int size() {
		return getData().length;
	}

	private Media toMedia() {

		return Media.builder()
			.mimeType(AUDIO_MP3)
			.data(getResource())
			.build();
	}

	public Document toDocument() {

		return Document.builder()
			.media(toMedia())
			.build();
	}

	public enum Category {
		LOSSLESS_COMPRESSED, LOSSY_COMPRESSED, UNCOMPRESSED
	}

	@Getter
	@ToString(of = "name")
	public enum Format {

		AAC("Advanced Audio Coding", Category.LOSSY_COMPRESSED),
		AIFF("Audio Interchange File Format", Category.UNCOMPRESSED),
		ALAC("Apple Lossless Audio Codec", Category.LOSSLESS_COMPRESSED),
		FLAC("Free Lossless Audio Codec", Category.LOSSLESS_COMPRESSED),
		MP3("MPEG-1 Audio Layer III", Category.LOSSLESS_COMPRESSED),
		WAV("Waveform Audio File Format", Category.UNCOMPRESSED);

		private final Category category;

		private final String name;

		Format(String name, Category category) {
			this.name = name;
			this.category = category;
		}

		public boolean isLossy() {
			return Category.LOSSY_COMPRESSED.equals(getCategory());
		}

		public boolean isLossless() {
			return Set.of(Category.UNCOMPRESSED, Category.LOSSLESS_COMPRESSED).contains(getCategory());
		}
	}
}
