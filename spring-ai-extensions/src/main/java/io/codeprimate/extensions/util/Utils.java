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
package io.codeprimate.extensions.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.cp.elements.lang.ObjectUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;

import io.codeprimate.extensions.spring.ai.chat.model.ChatModelWrapper;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Abstract utility class containing common, useful functions for Spring AI, Micrometer and Java.
 *
 * @author John Blum
 */
@SuppressWarnings("unused")
public abstract class Utils {

	public static final String EMPTY_STRING = "";
	public static final String SINGLE_SPACE = " ";

	public static <T, S> BiFunction<T, S, T> biFunctionReturnArgumentOne() {
		return (argumentOne, argumentTwo) -> argumentOne;
	}

	public static <T, S> BiFunction<T, S, S> biFunctionReturnArgumentTwo() {
		return (argumentOne, argumentTwo) -> argumentTwo;
	}

	public static ChatOptions buildChatOptions(String model) {
		return ChatOptionsBuilder.builder().withModel(model).build();
	}

	public static <T> T defaultIfNull(T value, T defaultValue) {
		return defaultIfNull(value, () -> defaultValue);
	}

	public static <T> T defaultIfNull(T value, Supplier<T> defaultValue) {
		return value != null ? value : defaultValue.get();
	}

	public static String generatedContent(ChatResponse chatResponse) {

		return Optional.ofNullable(chatResponse)
			.map(ChatResponse::getResult)
			.map(Utils::generatedContent)
			.orElse(EMPTY_STRING);
	}

	public static String generatedContent(Generation generation) {

		return Optional.ofNullable(generation)
			.map(Generation::getOutput)
			.map(Utils::generatedContent)
			.orElse(EMPTY_STRING);
	}

	public static String generatedContent(Message message) {

		return Optional.ofNullable(message)
			.map(Message::getText)
			.orElse(EMPTY_STRING);
	}

	public static boolean isNotEmpty(float[] array) {
		return array != null && array.length > 0;
	}

	public static boolean isNotEmpty(Iterable<?> iterable) {
		return iterable != null && iterable.iterator().hasNext();
	}

	public static <T> Consumer<T> nullSafeConsumer(Consumer<T> consumer) {
		return consumer != null ? consumer : target -> {};
	}

	public static <T> Iterable<T> nullSafeIterable(Iterable<T> iterable) {
		return iterable != null ? iterable : Collections::emptyIterator;
	}

	public static <T> Iterator<T> nullSafeIterator(Iterator<T> iterator) {
		return iterator != null ? iterator : Collections.emptyIterator();
	}

	public static <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	public static <T> Set<T> nullSafeSet(Set<T> set) {
		return set != null ? set : Collections.emptySet();
	}

	public static String nullSafeString(String value) {
		return String.valueOf(value);
	}

	public static String nullSafeTypeName(Object target) {
		return nullSafeString(ObjectUtils.getClassName(target));
	}

	public static <T extends Meter> Optional<T> meterFrom(MeterRegistry meterRegistry,
			String meterName, Class<T> meterType) {

		return meterRegistry.getMeters().stream()
			.filter(meter -> meter.getId().getName().equalsIgnoreCase(meterName))
			.filter(meterType::isInstance)
			.map(meterType::cast)
			.findFirst();
	}

	public static String promptContent(Prompt prompt) {
		return prompt != null ? prompt.getContents() : EMPTY_STRING;
	}

	public static ChatModel resolveChatModel(ChatModel chatModel) {

		return chatModel instanceof ChatModelWrapper chatModelWrapper
			? chatModelWrapper.getChatModel()
			: chatModel;
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return StreamSupport.stream(nullSafeIterable(iterable).spliterator(), false);
	}
}
