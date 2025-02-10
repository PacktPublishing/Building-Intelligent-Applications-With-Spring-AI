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
package io.codeprimate.extensions.spring.ai.chat.client.advisor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import reactor.core.publisher.Flux;

/**
 * Spring AI {@link CallAroundAdvisor} and {@link StreamAroundAdvisor} enforcing a configured rate limit
 * when making requests to an AI model.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class RateLimitAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	public static final int DEFAULT_COUNT = Integer.MAX_VALUE;
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1_000;

	public static final Duration DEFAULT_DURATION = Duration.ofDays(365);

	public static RateLimitAdvisor countPerSecond(int count) {
		return countPerDuration(count, Duration.ofSeconds(1L));
	}

	public static RateLimitAdvisor countPerMinute(int count) {
		return countPerDuration(count, Duration.ofMinutes(1L));
	}

	public static RateLimitAdvisor countPerHour(int count) {
		return countPerDuration(count, Duration.ofHours(1L));
	}

	public static RateLimitAdvisor countPerDuration(int count, Duration duration) {
		return new RateLimitAdvisor(count, duration);
	}

	public static RateLimitAdvisor onceEvery(Duration duration) {
		return new RateLimitAdvisor(1, duration);
	}

	private final int count;

	@Setter(AccessLevel.PROTECTED)
	private volatile long timestamp;

	private final AtomicInteger counter = new AtomicInteger(0);

	private final Duration duration;

	public RateLimitAdvisor() {
		this(DEFAULT_COUNT, DEFAULT_DURATION);
	}

	@Builder
	public RateLimitAdvisor(int count, Duration duration) {

		Assert.isTrue(count > 0 , "Count [%d] must be greater than equal to 1".formatted(count));
		Assert.notNull(duration, "Duration is required");

		this.count = count;
		this.duration = duration;
	}

	@Override
	public @NonNull String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

		Function<ChatResponse, AdvisedResponse> advisedResponseFunction = chatResponse ->
			buildAdvisedResponse(advisedRequest, chatResponse);

		Supplier<AdvisedResponse> advisedResponseSupplier = () -> chain.nextAroundCall(advisedRequest);

		return advisedResponse(System.currentTimeMillis(), advisedResponseFunction, advisedResponseSupplier);
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

		Function<ChatResponse, Flux<AdvisedResponse>> advisedResponseFunction = chatResponse ->
			Flux.just(buildAdvisedResponse(advisedRequest, chatResponse));

		Supplier<Flux<AdvisedResponse>> advisedResponseSupplier = () -> chain.nextAroundStream(advisedRequest);

		return advisedResponse(System.currentTimeMillis(), advisedResponseFunction, advisedResponseSupplier);
	}

	protected <T> T assertRateLimit(long currentTimeMillis, Supplier<T> advisedResponseSupplier) {

		if (isRateLimitExceeded(currentTimeMillis)) {
			throw RateLimitException.from(getCount(), getDuration(), getCounter().get());
		}

		return advisedResponseSupplier.get();
	}

	protected <T> T advisedResponse(long currentTimeMillis, Function<ChatResponse, T> advisedResponseFunction,
			Supplier<T> advisedResponseSupplier) {

		if (isRateLimitExceeded(currentTimeMillis)) {

			AssistantMessage assistantMessage = new AssistantMessage(message());
			Generation generation = new Generation(assistantMessage);

			ChatResponse chatResponse = ChatResponse.builder()
				.generations(List.of(generation))
				.build();

			return advisedResponseFunction.apply(chatResponse);
		}

		return advisedResponseSupplier.get();
	}

	protected AdvisedResponse buildAdvisedResponse(AdvisedRequest request, ChatResponse response) {

		return AdvisedResponse.builder()
			.response(response)
			.adviseContext(request.adviseContext())
			.build();
	}

	protected String message() {
		return RateLimitException.MESSAGE.formatted(getCount(), getDuration().toMillis(), getCounter().get());
	}

	protected boolean isRateLimitExceeded(long currentTimeMillis) {

		long expirationTimeMillis = getDuration().plusMillis(getTimestamp()).toMillis();

		if (currentTimeMillis < expirationTimeMillis) {
			long currentCount = getCounter().incrementAndGet();
			return currentCount > getCount();
		}
		else {
			setTimestamp(currentTimeMillis);
			getCounter().set(1);
			return false;
		}
	}
}
