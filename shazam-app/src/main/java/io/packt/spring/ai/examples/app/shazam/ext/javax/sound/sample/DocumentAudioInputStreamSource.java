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
package io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample;

import static io.packt.spring.ai.examples.app.shazam.util.NumberUtils.asLong;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.repo.AbstractDocumentStore;
import io.packt.spring.ai.examples.app.shazam.support.AudioAccessException;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.Builder;
import org.springframework.ai.document.Document;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Spring AI {@link Document} based {@link AudioInputStreamSource}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioInputStreamSource
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class DocumentAudioInputStreamSource implements AudioInputStreamSource {

	public static final String AUDIO_BYTE_OFFSET_KEY = "byteOffset";
	public static final String AUDIO_TIMESTAMP_KEY = "timestamp";

	protected static final int DEFAULT_AUDIO_BYTE_OFFSET = 0;

	public static DocumentBuilder builder(Audio audio) {
		return new DocumentAudioInputStreamSourceBuilder(audio);
	}

	private final Audio audio;
	private final Document document;

	protected DocumentAudioInputStreamSource(Audio audio, Document document) {
		this.audio = AudioUtils.assertAudio(audio);
		this.document = assertDocument(document);
	}

	@SuppressWarnings("all")
	private Document assertDocument(Document document) {

		Assert.notNull(document, "Document is required");
		Assert.isTrue(document instanceof AbstractDocumentStore.AudioDocument || document.getMedia() != null);
		Assert.notNull(document.getMedia().getDataAsByteArray(), "Document must have audio data");
		Assert.notEmpty(Arrays.asList(document.getMedia().getDataAsByteArray()), "Document must have audio data");

		return document;
	}

	@Override
	public AudioInputStream get() {

		try {
			AudioInputStream in = AudioUtils.openInputStream(audio);
			AudioFormat audioFormat = in.getFormat();
			in.readNBytes(resolveByteOffset(getDocument()));
			//in.skipNBytes(resolveByteOffset(getDocument()));
			long frameLength = computeFrameLength(getDocument(), audioFormat);
			return new AudioInputStream(in, audioFormat, frameLength);
		}
		catch (IOException cause) {
			String message = "";
			throw AudioAccessException.because(message, cause);
		}
	}

	protected long computeFrameLength(Document document, AudioFormat audioFormat) {
		int frameSize = audioFormat.getFrameSize();
		long audioSize = resolveAudioSize(document);
		return audioSize / frameSize;
	}

	private long resolveAudioSize(Document document) {

		return document instanceof AbstractDocumentStore.AudioDocument audioDocument
			? audioDocument.getAudio().size()
			: resolveDataLength(document);
	}

	protected int resolveByteOffset(Document document) {
		Object byteOffset = document.getMetadata().get(AUDIO_BYTE_OFFSET_KEY);
		return byteOffset instanceof Number number ? number.intValue()
			: DEFAULT_AUDIO_BYTE_OFFSET;
	}

	@SuppressWarnings("all")
	protected long resolveDataLength(Document document) {
		return ExceptionThrowingSupplier.getSafely(() -> asLong(document.getMedia().getDataAsByteArray().length),
			cause -> getAudio().size());
	}

	public interface DocumentBuilder {
		Builder<AudioInputStreamSource> using(Document document);
	}

	@Getter(AccessLevel.PROTECTED)
	protected static class DocumentAudioInputStreamSourceBuilder
			implements DocumentBuilder, Builder<AudioInputStreamSource> {

		private final Audio audio;
		private Document document;

		protected DocumentAudioInputStreamSourceBuilder(Audio audio) {
			this.audio = audio;
		}

		@Override
		public Builder<AudioInputStreamSource> using(Document document) {
			this.document = document;
			return this;
		}

		@Override
		public DocumentAudioInputStreamSource build() {
			return new DocumentAudioInputStreamSource(getAudio(), getDocument());
		}
	}
}
