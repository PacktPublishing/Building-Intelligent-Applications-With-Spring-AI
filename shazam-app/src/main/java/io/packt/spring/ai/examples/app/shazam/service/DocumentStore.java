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

import io.codeprimate.extensions.spring.ai.document.DocumentNotFoundException;

import org.springframework.ai.document.Document;

/**
 * Strategy interface defining a contract for saving and retrieving Spring AI {@link Document Docuemnts}
 * for subsequent processing in an application workflow (processing pipeline).
 *
 * @author John Blum
 * @see Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface DocumentStore {

	default Document get(String id) {
		throw DocumentNotFoundException.forDocumentId(id);
	}

	boolean isEmpty();

	default boolean isNotEmpty() {
		return !isEmpty();
	}

	boolean remove(Document document);

	Document store(Document document);

	long size();

}
