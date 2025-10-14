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
 * {@link Enum Enumeration} of functions performed by an AI {@link Model}.
 *
 * @author John Blum
 * @see org.springframework.ai.model.Model
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public enum ModelFunction {

	AGENT,
	AUDIO_TRANSCRIPTION,
	CHAT,
	EMBEDDING,
	FUNCTION_CALLING,
	MODERATION,
	SPEECH_TO_TEXT,
	TEXT_TO_SPEECH("TTS"),
	VISION;

	@Getter
	private final String acronym;

	ModelFunction() {
		this(null);
	}

	ModelFunction(String acronym) {
		this.acronym = acronym;
	}

	public boolean isAgent() {
		return this.equals(AGENT);
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

	public boolean isFunctionCalling() {
		return this.equals(FUNCTION_CALLING);
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

	public boolean isVision() {
		return this.equals(VISION);
	}
}
