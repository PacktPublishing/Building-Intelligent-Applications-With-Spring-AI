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
package com.packt.spring.ai.examples.connect4.model;

import io.codeprimate.extensions.spring.ai.provider.AiProvider;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) modeling a single player in {@literal Connect4}.
 *
 * @author John Blum
 * @param provider {@link AiProvider} model used as the {@literal player}.
 * @param disc {@link Disc} played by the {@literal player}.
 * @see AiProvider
 * @see Disc
 */
public record Player(AiProvider provider, Disc disc) {

	public Player {
		Assert.notNull(provider, "AI Provider is required");
		Assert.notNull(disc, "Disc is required");
	}

	public static Builder from(AiProvider provider) {
		return new Builder(provider);
	}

	public String getName() {
		return provider().getName();
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Builder {

		private final AiProvider provider;

		public Player playing(Disc disc) {
			return new Player(getProvider(), disc);
		}
	}
}
