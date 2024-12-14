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
package io.codeprimate.extensions.spring.ai.chat.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.AiProviders;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProviderModel;
import io.codeprimate.extensions.util.Utils;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Spring AI {@link ChatModel} implementation composed of 1 or more {@link ChatModel ChatModels}.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see io.codeprimate.extensions.spring.ai.provider.AiProviders
 * @see io.codeprimate.extensions.spring.ai.provider.support.SpringAiProviderModel
 * @see org.springframework.ai.chat.model.ChatModel
 * @see <a href="https://en.wikipedia.org/wiki/Composite_pattern">Composite Software Design Pattern</a>
 */
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class CompositeChatModel implements Iterable<ChatModel>, ChatModel {

	public static CompositeChatModel empty() {
		return of();
	}

	public static CompositeChatModel of(ChatModel... chatModels) {
		return of(Arrays.asList(chatModels));
	}

	public static CompositeChatModel of(Iterable<ChatModel> chatModels) {
		return new CompositeChatModel(Utils.stream(chatModels).toList());
	}

	private final AiProviders aiProviders;

	private volatile ChatModel currentChatModel;

	private final Set<ChatModel> chatModels;

	public CompositeChatModel(List<ChatModel> chatModels) {

		List<ChatModel> resolvedChatModels = resolveChatModels(chatModels);

		this.aiProviders = resolveAiProviders(resolvedChatModels);
		this.chatModels = new HashSet<>(resolvedChatModels);
		this.currentChatModel = resolveCurrentChatModel(resolvedChatModels);
	}

	private AiProviders resolveAiProviders(List<ChatModel> chatModels) {

		List<AiProvider> aiProviderList = chatModels.stream()
			.map(SpringAiProviderModel::from)
			.map(AiProvider.class::cast)
			.toList();

		return AiProviders.of(aiProviderList);
	}

	private List<ChatModel> resolveChatModels(List<ChatModel> chatModels) {

		return Utils.nullSafeList(chatModels).stream()
			.filter(Objects::nonNull)
			.toList();
	}

	private ChatModel resolveCurrentChatModel(List<ChatModel> chatModels) {
		return chatModels.stream().findFirst().orElse(null);
	}

	public ChatModel getCurrentChatModel() {

		Assert.state(this.currentChatModel != null, "ChatModel has not been initialized;"
			+ " Did you include a ChatModel implementation on your application classpath?");

		return this.currentChatModel;
	}

	protected Optional<ChatModel> getOptionalCurrentChatModel() {
		return Optional.ofNullable(this.currentChatModel);
	}

	@Override
	public ChatOptions getDefaultOptions() {

		return getOptionalCurrentChatModel()
			.map(ChatModel::getDefaultOptions)
			.orElseGet(ChatOptionsBuilder.builder()::build);
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		return getCurrentChatModel().call(prompt);
	}

	public Optional<ChatModel> findBy(Predicate<ChatModel> predicate) {
		return stream().filter(predicate).findFirst();
	}

	public ChatModel requireBy(Predicate<ChatModel> predicate) {
		return findBy(predicate).orElseThrow(() ->
			new ChatModelNotFoundException("ChatModel could not be found with Predicate [%s]".formatted(predicate)));
	}

	@Override
	public @NonNull Iterator<ChatModel> iterator() {
		return Collections.unmodifiableSet(getChatModels()).iterator();
	}

	public Stream<ChatModel> stream() {
		return Utils.stream(this);
	}

	public CompositeChatModel use(AiProvider aiProvider) {

		Assert.notNull(aiProvider, "AI provider to use is required");

		Predicate<AiProvider> matchingAiProviderPredicate = configuredAiProvider ->
			((SpringAiProviderModel) configuredAiProvider).aiProvider().equals(aiProvider);

		getAiProviders().findBy(matchingAiProviderPredicate)
			.map(SpringAiProviderModel.class::cast)
			.<ChatModel>map(SpringAiProviderModel::getTypedModel)
			.ifPresent(this::use);

		return this;
	}

	public CompositeChatModel use(ChatModel chatModel) {

		Assert.notNull(chatModel, "ChatModel is required");

		getChatModels().add(chatModel);
		this.currentChatModel = chatModel;

		return this;
	}

	public CompositeChatModel use(Class<ChatModel> chatModelType) {

		Assert.notNull(chatModelType, "Type of ChatModel is required");

		Predicate<ChatModel> chatModelByType = chatModelType::isInstance;
		ChatModel chatModel = requireBy(chatModelByType);

		return use(chatModel);
	}
}
