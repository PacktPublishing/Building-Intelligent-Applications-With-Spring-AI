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

import java.time.Duration;

import org.cp.elements.lang.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) and Java Record modeling a {@link Play} made by a {@link Player} in {@literal Connect4}.
 *
 * @author John Blum
 * @param player {@link Player} that made the {@link Play}.
 * @param play {@link Play action} performed by the {@link Player}.
 * @see Player
 * @see Play
 */
@SuppressWarnings("unused")
public record PlayerAction(Player player, Play play, Duration time) {

	public static final Duration DEFAULT_TIME = Duration.ZERO;

	public PlayerAction {
		Assert.notNull(player, "Player is required");
		Assert.notNull(play, "Play is required");
	}

	public static Builder by(Player player) {
		return new Builder(player);
	}

	public PlayerAction in(Duration time) {
		Assert.notNull(time, "Duration is required");
		Assert.isTrue(time.compareTo(Duration.ZERO) > 0, "Duration [%s] must greater than 0".formatted(time));
		return new PlayerAction(player(), play(), time);
	}

	public String move() {
		return play().move();
	}

	public String reason() {
		return play().explanation();
	}

	@Getter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Builder {

		private final Player player;

		public PlayerAction played(Play play) {
			return new PlayerAction(getPlayer(), play, DEFAULT_TIME);
		}
	}
}
