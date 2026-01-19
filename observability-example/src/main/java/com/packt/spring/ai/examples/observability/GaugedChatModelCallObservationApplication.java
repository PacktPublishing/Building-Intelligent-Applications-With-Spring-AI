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
package com.packt.spring.ai.examples.observability;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.codeprimate.extensions.micrometer.observation.ChatModelActiveCallCountObservationHandler;
import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.util.ExceptionThrowingRunnable;
import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to demonstrate Observability with Micrometer.
 *
 * @author John Blum
 * @see java.util.concurrent.ExecutorService
 * @see io.codeprimate.extensions.micrometer.observation.ChatModelActiveCallCountObservationHandler
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see io.micrometer.core.instrument.Gauge
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@EnableScheduling
@Profile(GaugedChatModelCallObservationApplication.SPRING_APPLICATION_PROFILE)
@SuppressWarnings("unused")
public class GaugedChatModelCallObservationApplication extends AbstractSpringBootApplication {

	protected static final String SPRING_APPLICATION_PROFILE = "gauged-chat-model-call-observation";
	protected static final String USER_PROFILE = "user";

	private static final int THREAD_COUNT = 3;

	private static final SecureRandom random = new SecureRandom();

	private static final Stack<String> PROMPTS = new Stack<>();

	static {
		PROMPTS.add("Generate a humorous dialog between a cat and dog");
		PROMPTS.add("Describe a journey through the Milky Way galaxy");
		PROMPTS.add("How would you invent time travel");
		PROMPTS.add("If you could have any job what would you do and why");
		PROMPTS.add("What are the downsides of being a super hero");
	}

	public static void main(String[] args) {

		new SpringApplicationBuilder(GaugedChatModelCallObservationApplication.class)
			.profiles(SPRING_APPLICATION_PROFILE, USER_PROFILE)
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

	@Bean
	ChatModelActiveCallCountObservationHandler observationHandler(MeterRegistry meterRegistry) {
		return new ChatModelActiveCallCountObservationHandler(meterRegistry);
	}

	@Bean
	ScheduledExecutorService observationMonitorScheduler() {

		return Executors.newSingleThreadScheduledExecutor(runner -> {
			Thread thread = new Thread(runner);
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY);
			return thread;
		});
	}

	@Service
	@RequiredArgsConstructor
	@Getter(AccessLevel.PROTECTED)
	static class ObservationMonitor {

		private final AtomicReference<Gauge> cachedGauge = new AtomicReference<>(null);

		private final MeterRegistry meterRegistry;

		@PreDestroy
		public void doFinal() {
			printActiveCallCount();
		}

		@Scheduled(fixedRate = 1L, timeUnit = TimeUnit.SECONDS, scheduler = "observationMonitorScheduler")
		public void observationRunner() {
			printActiveCallCount();
		}

		private void printActiveCallCount() {
			print("Active Call Count [%d]%n", resolveActiveCallCount());
		}

		private long resolveActiveCallCount() {

			Gauge gauge = getCachedGauge().updateAndGet(meter -> meter != null ? meter
				: resolveMeter(ChatModelActiveCallCountObservationHandler.METER_NAME));

			return gauge != null ? Double.valueOf(gauge.value()).longValue() : 0L;
		}

		private Gauge resolveMeter(String meterName) {

			return getMeterRegistry().getMeters().stream()
				.filter(meter -> meter.getId().getName().equalsIgnoreCase(meterName))
				.filter(Gauge.class::isInstance)
				.map(Gauge.class::cast)
				.findFirst()
				.orElse(null);
		}
	}

	@Bean
	@SuppressWarnings("all")
	ApplicationRunner programRunner(ChatClient chatClient, MeterRegistry meterRegistry) {

		return args -> {

			ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

			List<Future<?>> futures = new ArrayList<>(PROMPTS.stream()
				.map(Prompt::new)
				.map(prompt -> promptRunner(chatClient, prompt))
				.map(executorService::submit)
				.toList());

			futures.forEach(this::block);

			executorService.shutdown();
		};
	}

	private void block(Future<?> future) {
		ExceptionThrowingSupplier.getSafely(future::get);
	}

	private void pause(Duration duration) {
		ExceptionThrowingRunnable.runSafely(() -> Thread.sleep(duration.toMillis()));
	}

	private Callable<String> promptRunner(ChatClient chatClient, Prompt prompt) {

		return () -> {
			pause(Duration.ofSeconds(random.nextInt(3)));
			print("Prompt [%s]%n", prompt.getContents());
			return chatClient.prompt(prompt).call().content();
		};
	}
}
