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

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;

import org.cp.elements.lang.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import lombok.Getter;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to implement the Connect 4 Game.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(ConnectFourApplication.CONNECT_FOUR_PROFILE)
@SuppressWarnings("unused")
public class ConnectFourApplication extends AbstractSpringBootApplication {

	private static final int CONNECT_FOUR = 4;

	private static final BiFunction<Integer, Integer, Integer> BI_FUNCTION_IDENTITY =
		(argumentOne, argumentTwo) -> argumentOne;

	protected static final String CONNECT_FOUR_PROFILE = "connect4";

	public static void main(String[] args) {
		printExampleGameBoard();
		//runSpringApplication(ConnectFourApplication.class, useProfiles(CONNECT_FOUR_PROFILE), args);
	}

	private static void printExampleGameBoard() {
		new ConnectFourBoardGame()
			.play(Disc.GOLD, 1)
			.play(Disc.RED, 1)
			.play(Disc.GOLD, 2)
			.play(Disc.RED, 3)
			.play(Disc.GOLD, 3)
			.printGameBoard();
	}

	static class ConnectFourBoardGame {

		private static final int ROWS = 6;
		private static final int COLUMNS = 7;

		private final Disc[][] gameBoard = new Disc[ROWS][COLUMNS];

		private final int[] columns = new int[COLUMNS];

		ConnectFourBoardGame() {

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				Arrays.fill(this.gameBoard[rowIndex], null);
			}

			Arrays.fill(this.columns, ROWS);
		}

		boolean isWinner() {
			return getWinner() != null;
		}

		@Nullable Disc getWinner() {

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
					Disc disc = this.gameBoard[rowIndex][columnIndex];
					if (disc != null) {
						if (Disc.exists(checkConnectFourUp(disc, rowIndex, columnIndex))) {
							return disc;
						}
						if (Disc.exists(checkConnectFourForward(disc, rowIndex, columnIndex))) {
							return disc;
						}
						if (Disc.exists(checkConnectFourDiagonally(disc, rowIndex, columnIndex))) {
							return disc;
						}
					}
				}
			}

			return null;
		}

		private boolean isBackwardPossible(int columnIndex) {
			// return columnIndex >= 2;
			return column(columnIndex) - CONNECT_FOUR >= 0;
		}

		private boolean isDiagonalPossible(int rowIndex, int columnIndex) {
			return isUpPossible(rowIndex) && (isBackwardPossible(columnIndex) || isForwardPossible(columnIndex));
		}

		private boolean isForwardPossible(int columnIndex) {
			return columnIndex + CONNECT_FOUR <= COLUMNS;
		}

		private boolean isUpPossible(int rowIndex) {
			return rowIndex + CONNECT_FOUR <= ROWS;
		}

		private Disc checkConnectFour(Disc disc, int rowIndex, int columnIndex,
				BiFunction<Integer, Integer, Integer> rowIndexFunction,
				BiFunction<Integer, Integer, Integer> columnIndexFunction) {

			for (int indexOffset = 1; indexOffset < CONNECT_FOUR; indexOffset++) {
				int nextRowIndex = rowIndexFunction.apply(rowIndex, indexOffset);
				int nextColumnIndex = columnIndexFunction.apply(columnIndex, indexOffset);
				Disc nextDisc = this.gameBoard[nextRowIndex][nextColumnIndex];
				if (!disc.equals(nextDisc)) {
					return null;
				}
			}

			return disc;
		}

		private Disc checkConnectFourDiagonally(Disc disc, int rowIndex, int columnIndex) {

			if (isUpPossible(rowIndex)) {
				if (isBackwardPossible(columnIndex)) {
					Disc winner = checkConnectFourDiagonallyBackward(disc, rowIndex, columnIndex);
					if (winner != null) {
						return winner;
					}
				}

				if (isForwardPossible(columnIndex)) {
					return checkConnectFourDiagonallyForward(disc, rowIndex, columnIndex);
				}
			}

			return null;
		}

		private Disc checkConnectFourDiagonally(Disc disc, int rowIndex, int columnIndex,
				BiFunction<Integer, Integer, Integer> columnIndexFunction) {
			return checkConnectFour(disc, rowIndex, columnIndex, Integer::sum, columnIndexFunction);
		}

		private Disc checkConnectFourDiagonallyBackward(Disc disc, int rowIndex, int columnIndex) {
			return checkConnectFourDiagonally(disc, rowIndex, columnIndex, this::subtract);
		}

		private Disc checkConnectFourDiagonallyForward(Disc disc, int rowIndex, int columnIndex) {
			return checkConnectFourDiagonally(disc, rowIndex, columnIndex, Integer::sum);
		}

		private Disc checkConnectFourForward(Disc disc, int rowIndex, int columnIndex) {
			return isForwardPossible(columnIndex)
				? checkConnectFour(disc, rowIndex, columnIndex, BI_FUNCTION_IDENTITY, Integer::sum)
				: null;
		}

		private Disc checkConnectFourUp(Disc disc, int rowIndex, int columnIndex) {

			return isUpPossible(rowIndex)
				? checkConnectFour(disc, rowIndex, columnIndex, Integer::sum, BI_FUNCTION_IDENTITY)
				: null;
		}

		int column(int columnIndex) {
			return columnIndex + 1;
		}

		int columnIndex(int column) {
			return column - 1;
		}

		int row(int rowIndex) {
			return rowIndex + 1;
		}

		int rowIndex(int row) {
			return row - 1;
		}

		int subtract(int valueOne, int valueTwo) {
			return valueOne - valueTwo;
		}

		ConnectFourBoardGame play(Disc disc, int column) {

			int columnIndex = columnIndex(column);
			int rowIndex = rowIndex(this.columns[columnIndex]);

			Assert.isTrue(rowIndex >= 0, () -> "Cannot play column [%d]".formatted(column));

			this.columns[columnIndex] = rowIndex;
			this.gameBoard[rowIndex][columnIndex] = disc;

			return this;
		}

		ConnectFourBoardGame printGameBoard() {
			System.out.println(gameBoardToString());
			return this;
		}

		@Override
		public String toString() {
			return gameBoardToString();
		}

		private String gameBoardToString() {

			StringBuilder stringBuilder = new StringBuilder(borderToString());

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				stringBuilder.append(rowToString(rowIndex));
				stringBuilder.append(borderToString());
			}

			return stringBuilder.toString();
		}

		private String rowToString(int rowIndex) {

			return IntStream.range(0, COLUMNS)
				.mapToObj(columnIndex -> columnToString(rowIndex, columnIndex))
				.reduce("%s%s"::formatted)
				.orElse(StringUtils.EMPTY_STRING)
				.concat("|\n");
		}

		private String columnToString(int rowIndex, int columnIndex) {
			Disc disc = this.gameBoard[rowIndex][columnIndex];
			return "| %s ".formatted(disc != null ? disc.getSymbol() : StringUtils.SINGLE_SPACE);
		}

		private String borderToString() {

			return IntStream.range(0, COLUMNS)
				.mapToObj(index -> " ---")
				.reduce("%s%s"::formatted)
				.orElse(StringUtils.EMPTY_STRING)
				.concat("\n");
		}
	}

	@Getter
	enum Disc {

		RED("X"), GOLD("O");

		static boolean exists(Disc disc) {
			return disc != null;
		}

		private final String symbol;

		Disc(String symbol) {
			this.symbol = StringUtils.requireText(symbol, "Symbol is required");
		}

	}
}
