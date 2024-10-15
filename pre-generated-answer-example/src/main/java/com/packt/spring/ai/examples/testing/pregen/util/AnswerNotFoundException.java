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
package com.packt.spring.ai.examples.testing.pregen.util;

import com.packt.spring.ai.examples.testing.pregen.model.Answer;
import com.packt.spring.ai.examples.testing.pregen.model.Question;

/**
 * Java {@link RuntimeException} thrown when a {@link Answer pre-generated answer} cannot be found.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @see com.packt.spring.ai.examples.testing.pregen.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregen.model.Question
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class AnswerNotFoundException extends RuntimeException {

	public static AnswerNotFoundException from(Question question) {
		return from(question, null);
	}

	public static AnswerNotFoundException from(Question question, Throwable cause) {
		return new AnswerNotFoundException("Answer to Question [%s] not found".formatted(question), cause);
	}

	public AnswerNotFoundException() { }

	public AnswerNotFoundException(String message) {
		super(message);
	}

	public AnswerNotFoundException(Throwable cause) {
		super(cause);
	}

	public AnswerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
