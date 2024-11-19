/*
 *  Copyright 2024 Author or Authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.codeprimate.extensions.micrometer.observation;

import java.time.Duration;
import java.util.List;

import io.micrometer.common.KeyValues;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.lang.NonNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Micrometer {@link ObservationHandler} used to measure latency of AI {@link ChatModel} interactions.
 *
 * @author John Blum
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see io.micrometer.core.instrument.Timer
 * @see io.micrometer.observation.Observation
 * @see io.micrometer.observation.ObservationHandler
 * @see org.springframework.ai.chat.model.ChatModel
 * @since 0.1.0
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ChatModelCallLatencyObservationHandler implements ObservationHandler<ChatModelObservationContext> {

	public static final String METER_NAME = "chat.model.call.latency";

	protected static final String METER_DESCRIPTION =
		"Metric measuring the latency between an AI model prompt and generated response";

	private final MeterRegistry meterRegistry;

	@Override
	public void onStart(ChatModelObservationContext observationContext) {
		observationContext.put("chat.model.call.start-time", System.currentTimeMillis());
	}

	@Override
	@SuppressWarnings("all")
	public void onStop(ChatModelObservationContext observationContext) {

		long endTime = System.currentTimeMillis();
		long startTime = observationContext.get("chat.model.call.start-time");
		long durationMillis = endTime - startTime;

		recordTime(observationContext, durationMillis);
	}

	protected void recordTime(ChatModelObservationContext observationContext, long durationMillis) {

		Timer.builder(METER_NAME)
			.description(METER_DESCRIPTION)
			.withRegistry(getMeterRegistry())
			.withTags(resolveTags(observationContext))
			.record(Duration.ofMillis(durationMillis));
	}

	protected KeyValues resolveKeyValue(ChatModelObservationContext observationContext) {
		return observationContext.getLowCardinalityKeyValues();
	}

	protected Tags resolveTags(ChatModelObservationContext observationContext) {

		List<Tag> tags = resolveKeyValue(observationContext).stream()
			.map(keyValue -> Tag.of(keyValue.getKey(), keyValue.getValue()))
			.toList();

		return Tags.of(tags);
	}

	@Override
	public boolean supportsContext(@NonNull Observation.Context context) {
		return context instanceof ChatModelObservationContext;
	}
}
