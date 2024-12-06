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
package com.packt.spring.ai.examples.connect4;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link ConnectFourApplication.ConnectFourBoardGame Connect 4}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class ConnectFourBoardGameUnitTests {

	@Test
	void gameBoardOne() {

		ConnectFourApplication.ConnectFourBoardGame boardGame = new ConnectFourApplication.ConnectFourBoardGame()
			.play(ConnectFourApplication.Disc.RED, 1)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 2)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 3)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 4)
			.printGameBoard();

		assertThat(boardGame.isWinner()).isTrue();
		assertThat(boardGame.getWinner()).isEqualTo(ConnectFourApplication.Disc.RED);
	}

	@Test
	void gameBoardTwo() {

		ConnectFourApplication.ConnectFourBoardGame boardGame = new ConnectFourApplication.ConnectFourBoardGame()
			.play(ConnectFourApplication.Disc.RED, 1)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 2)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 3)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.printGameBoard();

		assertThat(boardGame.isWinner()).isFalse();
		assertThat(boardGame.getWinner()).isNull();
	}

	@Test
	void gameBoardThree() {

		ConnectFourApplication.ConnectFourBoardGame boardGame = new ConnectFourApplication.ConnectFourBoardGame()
			.play(ConnectFourApplication.Disc.RED, 1)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 2)
			.play(ConnectFourApplication.Disc.GOLD, 2)
			.play(ConnectFourApplication.Disc.RED, 1)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 3)
			.play(ConnectFourApplication.Disc.GOLD, 4)
			.play(ConnectFourApplication.Disc.RED, 7)
			.play(ConnectFourApplication.Disc.GOLD, 2)
			.play(ConnectFourApplication.Disc.RED, 7)
			.play(ConnectFourApplication.Disc.GOLD, 3)
			.printGameBoard();

		assertThat(boardGame.isWinner()).isTrue();
		assertThat(boardGame.getWinner()).isEqualTo(ConnectFourApplication.Disc.GOLD);
	}

	@Test
	void gameBoardFour() {

		ConnectFourApplication.ConnectFourBoardGame boardGame = new ConnectFourApplication.ConnectFourBoardGame()
			.play(ConnectFourApplication.Disc.RED, 1)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 2)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 3)
			.play(ConnectFourApplication.Disc.GOLD, 4)
			.play(ConnectFourApplication.Disc.RED, 7)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.play(ConnectFourApplication.Disc.RED, 7)
			.play(ConnectFourApplication.Disc.GOLD, 1)
			.printGameBoard();

		assertThat(boardGame.isWinner()).isTrue();
		assertThat(boardGame.getWinner()).isEqualTo(ConnectFourApplication.Disc.GOLD);
	}
}
