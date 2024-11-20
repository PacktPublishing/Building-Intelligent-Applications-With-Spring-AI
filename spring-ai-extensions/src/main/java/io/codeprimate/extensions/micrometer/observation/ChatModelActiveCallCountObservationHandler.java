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

import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.lang.NonNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Micrometer {@link ObservationHandler} used to measure the number of active AI {@link ChatModel} interactions
 * at a given moment.
 *
 * @author John Blum
 * @see io.micrometer.observation.ObservationHandler
 * @since 0.1.0
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ChatModelActiveCallCountObservationHandler implements ObservationHandler<ChatModelObservationContext>{

	public static final String METER_NAME = "chat.model.active.call.count";

	protected static final String DESCRIPTION = "Meter used to record the number of active AI ChatModel interactions";

	private final AtomicLong activeCallCount = new AtomicLong(0L);

	private final MeterRegistry meterRegistry;

	@Override
	public void onStart(@NonNull ChatModelObservationContext observationContext) {

		Gauge.builder(METER_NAME, () -> activeCallCount)
			.description(DESCRIPTION)
			.tag("timestamp", String.valueOf(System.currentTimeMillis()))
			.register(meterRegistry);

		getActiveCallCount().incrementAndGet();
	}

	@Override
	public void onStop(ChatModelObservationContext context) {
		getActiveCallCount().decrementAndGet();
	}

	@Override
	public boolean supportsContext(@NonNull Observation.Context context) {
		return context instanceof ChatModelObservationContext;
	}
}
