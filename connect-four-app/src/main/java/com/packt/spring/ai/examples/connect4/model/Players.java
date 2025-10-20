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

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.util.Utils;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract Data Type (ADT) modeling 2 {@link Player Players} in {@literal Connect4}.
 *
 * @author John Blum
 * @see Iterable
 * @see Player
 * @since 0.1.0
 */
@Getter
public abstract class Players implements Iterable<Player> {

	public static Players of(Player one, Player two) {

		Assert.notNull(one, "Player One is required");
		Assert.notNull(two, "Player Two is required");

		return new Players() {

			@Override
			public Player one() {
				return one;
			}

			@Override
			public Player two() {
				return two;
			}
		};
	}

	@Setter(AccessLevel.PROTECTED)
	private Player currentPlayer = one();

	public abstract Player one();

	public abstract Player two();

	public Player findByDisc(Disc disc) {

		return stream()
			.filter(player -> player.disc().equals(disc))
			.findFirst()
			.orElseThrow(() -> {
				String message = "Player for Disc [%s] not found".formatted(disc);
				return new IllegalStateException(message);
			});
	}

	@Override
	public @NonNull Iterator<Player> iterator() {
		return List.of(one(), two()).iterator();
	}

	public Player startingPlayer(SecureRandom random, CompositeChatModel chatModel) {
		Player startingPlayer = random.nextInt(100) % 2 == 0 ? two() : one();
		AiProvider provider = startingPlayer.provider();
		chatModel.use(provider);
		setCurrentPlayer(startingPlayer);
		return startingPlayer;
	}

	public Player switchPlayer(CompositeChatModel chatModel) {
		Player nextPlayer = getCurrentPlayer().equals(one()) ? two() : one();
		AiProvider aiProvider = nextPlayer.provider();
		chatModel.use(aiProvider);
		setCurrentPlayer(nextPlayer);
		return getCurrentPlayer();
	}

	public Stream<Player> stream() {
		return Utils.stream(this);
	}
}
