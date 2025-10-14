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
package io.codeprimate.extensions.spring.ai.provider.model;

import org.springframework.ai.model.Model;

import lombok.Getter;

/**
 * {@link Enum Enumeration} of input and output types of AI provider {@link Model Models}.
 *
 * @author John Blum
 * @see org.springframework.ai.model.Model
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum Modality {

	AUDIO,
	AUDIO_TRANSCRIPTION,
	CHAT,
	EMBEDDING,
	IMAGE,
	MODERATION,
	SPEECH_TO_TEXT,
	TEXT,
	TEXT_TO_SPEECH("TTS"),
	VIDEO,
	VISION;

	@Getter
	private final String acronym;

	Modality() {
		this(null);
	}

	Modality(String acronym) {
		this.acronym = acronym;
	}

	public boolean isAudio() {
		return this.equals(AUDIO);
	}

	public boolean isAudioTranscription() {
		return this.equals(AUDIO_TRANSCRIPTION);
	}

	public boolean isChat() {
		return this.equals(CHAT);
	}

	public boolean isEmbedding() {
		return this.equals(EMBEDDING);
	}

	public boolean isImage() {
		return this.equals(IMAGE);
	}

	public boolean isModeration() {
		return this.equals(MODERATION);
	}

	public boolean isSpeechToText() {
		return this.equals(SPEECH_TO_TEXT);
	}

	public boolean isTextToSpeed() {
		return this.equals(TEXT_TO_SPEECH);
	}

	public boolean isVideo() {
		return this.equals(VIDEO);
	}

	public boolean isVision() {
		return this.equals(VISION);
	}

	public String getKeyword() {
		return name().toLowerCase();
	}
}
