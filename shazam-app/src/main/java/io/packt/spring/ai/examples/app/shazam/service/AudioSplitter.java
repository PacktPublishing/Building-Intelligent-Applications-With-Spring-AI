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
package io.packt.spring.ai.examples.app.shazam.service;

import java.util.List;

import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.model.AudioSource;

import org.springframework.ai.document.Document;

/**
 * Interface defining a contract used to split up audio data into chunks as individual {@link Document documents}
 * that can then be stored in a {@link org.springframework.ai.vectorstore.VectorStore}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioSource
 * @see Document
 * @since 0.1.0
 */
@FunctionalInterface
public interface AudioSplitter {

	List<Document> split(Audio audio);

	default List<Document> split(AudioSource source) {
		return split(source.getAudio());
	}
}
