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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.util;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;

/**
 * Java {@link RuntimeException} thrown when {@link Question questions} cannot be {@link Answer answered}.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 */
@SuppressWarnings("unused")
public class UnknownAnswerException extends RuntimeException {

	public static UnknownAnswerException from(Question question) {
		return new UnknownAnswerException("Question [%s] cannot be answered".formatted(question));
	}

	public UnknownAnswerException() { }

	public UnknownAnswerException(String message) {
		super(message);
	}

	public UnknownAnswerException(Throwable cause) {
		super(cause);
	}

	public UnknownAnswerException(String message, Throwable cause) {
		super(message, cause);
	}
}
