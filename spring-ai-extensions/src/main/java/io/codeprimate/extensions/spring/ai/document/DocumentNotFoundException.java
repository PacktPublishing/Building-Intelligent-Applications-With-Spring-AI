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
package io.codeprimate.extensions.spring.ai.document;

import org.springframework.ai.document.Document;

/**
 * Java {@link RuntimeException} thrown when a {@link Document} cannot be found.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class DocumentNotFoundException extends RuntimeException {

	public static DocumentNotFoundException forDocumentId(String documentId) {
		return new DocumentNotFoundException("Document with ID [%s] not found".formatted(documentId));
	}

	public DocumentNotFoundException() { }

	public DocumentNotFoundException(String message) {
		super(message);
	}

	public DocumentNotFoundException(Throwable cause) {
		super(cause);
	}

	public DocumentNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
