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
package com.packt.spring.ai.examples.observability;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to demonstrate Observability with Micrometer
 * to measure the latency of chat model interactions.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see io.micrometer.core.instrument.Timer
 * @see io.micrometer.observation.Observation
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 */
@SpringBootApplication
@Profile(TimedChatModelCallObservationApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class TimedChatModelCallObservationApplication extends AbstractSpringBootApplication {

	protected static final String SPRING_APPLICATION_PROFILE = "timed-chat-model-call-observation";

	public static void main(String[] args) {

		new SpringApplicationBuilder(TimedChatModelCallObservationApplication.class)
			.profiles(SPRING_APPLICATION_PROFILE)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}

	@Bean
	SimpleMeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

	@Component
	@RequiredArgsConstructor
	@Getter(AccessLevel.PROTECTED)
	static class ChatModelLatencyObservationHandler implements ObservationHandler<ChatModelObservationContext> {

		private final MeterRegistry meterRegistry;

		@Override
		public void onStart(ChatModelObservationContext context) {
			context.put("chat.model.call.start-time", System.currentTimeMillis());
		}

		@Override
		@SuppressWarnings("all")
		public void onStop(ChatModelObservationContext context) {

			long endTime = System.currentTimeMillis();
			long startTime = context.get("chat.model.call.start-time");
			long durationMillis = endTime - startTime;

			Timer.builder("chat.model.call.latency")
				.description("Metric measuring the latency betwen an AI model prompt and generated response")
				.withRegistry(getMeterRegistry())
				.withTags()
				.record(Duration.ofMillis(durationMillis));
		}

		@Override
		public boolean supportsContext(@NonNull Observation.Context context) {
			return context instanceof ChatModelObservationContext;
		}
	}

	@Bean
	ApplicationRunner programRunner(ChatClient chatClient, MeterRegistry meterRegistry) {

		return readEvaluatePrintLoop((args, input) -> {

			Prompt prompt = new Prompt(input);

			ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

			outputAiResponse(chatResponse);

			TimeMeasurement timeMeasurement = getObservedLatency(meterRegistry);

			print("Chat Model Call:%n");
			print("Count         [%d]%n", timeMeasurement.getCount());
			print("Mean Latency  [%s ms]%n", timeMeasurement.getMean());
			print("Total Latency [%s ms]%n%n", timeMeasurement.getTotal());
		});
	}

	private TimeMeasurement getObservedLatency(MeterRegistry meterRegistry) {

		return meterRegistry.getMeters().stream()
			.filter(meter -> "chat.model.call.latency".equalsIgnoreCase(meter.getId().getName()))
			.findFirst()
			.filter(Timer.class::isInstance)
			.map(Timer.class::cast)
			.map(TimeMeasurement::measured)
			.orElseGet(TimeMeasurement::zero);
	}

	@Getter
	@Builder
	static class TimeMeasurement {

		static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

		static TimeMeasurement measured(Timer timer) {

			return TimeMeasurement.builder()
				.count(timer.count())
				.max(timer.max(DEFAULT_TIME_UNIT))
				.mean(timer.mean(DEFAULT_TIME_UNIT))
				.total(timer.totalTime(DEFAULT_TIME_UNIT))
				.build();
		}

		static TimeMeasurement zero() {

			return TimeMeasurement.builder()
				.count(0)
				.max(0.0d)
				.mean(0.0d)
				.total(0.0d)
				.build();
		}

		private double max;
		private double mean;
		private double total;
		private long count;

		private long asLong(double value) {
			return Double.valueOf(value).longValue();
		}

		TimeMeasurement toSeconds() {

			return TimeMeasurement.builder()
				.count(this.getCount())
				.max(TimeUnit.SECONDS.convert(asLong(this.getMax()), DEFAULT_TIME_UNIT))
				.mean(TimeUnit.SECONDS.convert(asLong(this.getMean()), DEFAULT_TIME_UNIT))
				.total(TimeUnit.SECONDS.convert(asLong(this.getTotal()), DEFAULT_TIME_UNIT))
				.build();
		}
	}
}
