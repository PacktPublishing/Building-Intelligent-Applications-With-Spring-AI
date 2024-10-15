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

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.packt.spring.ai.examples.testing.pregen.model.Question;

import org.springframework.ai.document.Document;
import org.springframework.util.Assert;

/**
 * Abstract utility class.
 *
 * @author John Blum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class Utils {

	public void assertEmbedding(Question question) {
		Assert.state(isEmbeddingPresent(question), () -> "Expected Embedding for Question [%s]".formatted(question));
	}

	public static boolean isEmbeddingPresent(Document document) {
		return document != null && isNotEmpty(document.getEmbedding());
	}

	public static boolean isEmbeddingPresent(Question question) {
		return question != null && isEmbeddingPresent(question.document());
	}

	public static boolean isNotEmpty(float[] array) {
		return array != null && array.length > 0;
	}

	public static boolean isNotEmpty(Iterable<?> iterable) {
		return iterable != null && iterable.iterator().hasNext();
	}

	public static <T> Consumer<T> consumeSafely(ExceptionThrowingConsumer<T> consumer) {

		return target -> {
			try {
				consumer.accept(target);
			}
			catch (Exception checked) {
				throw new RuntimeException(checked);
			}
		};
	}

	public static <T> Iterable<T> nullSafeIterable(Iterable<T> iterable) {
		return iterable != null ? iterable : Collections::emptyIterator;
	}

	public static void print(String message, Object... arguments) {
		System.out.printf(message, arguments);
		System.out.flush();
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return StreamSupport.stream(nullSafeIterable(iterable).spliterator(), false);
	}

	public interface ExceptionThrowingConsumer<T> {
		void accept(T target) throws Exception;
	}
}
