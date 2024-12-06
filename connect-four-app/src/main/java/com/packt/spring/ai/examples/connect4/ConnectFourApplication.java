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

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalStateException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;

import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.stream.StreamUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to implement the Connect 4 Game.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.boot.ApplicationRunner
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
		runSpringApplication(ConnectFourApplication.class, useProfiles(CONNECT_FOUR_PROFILE), args);
		//printExampleGameBoard();
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

	@SpringBootConfiguration
	@EnableChatClient
	static class ConnectFourConfiguration {

	}

	@Bean
	ApplicationRunner playGame(ChatClient chatClient, CompositeChatModel chatModel) {

		return args -> {

		};
	}

	@Getter(AccessLevel.PROTECTED)
	static class ConnectFourBoardGame {

		private static final int ROWS = 6;
		private static final int COLUMNS = 7;

		private final Columns columns;

		private final Disc[][] gameBoard = new Disc[ROWS][COLUMNS];

		ConnectFourBoardGame() {

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				Arrays.fill(this.gameBoard[rowIndex], null);
			}

			// Not Thread-safe; this reference escapes!
			this.columns = Columns.from(this);
		}

		Columns getPlayableColumns() {
			return getColumns().findPlayableColumns();
		}

		boolean isWinner() {
			return getWinner() != null;
		}

		boolean isNotWinner() {
			return !isWinner();
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
			return Column.asColumnNumber(columnIndex) - CONNECT_FOUR >= 0;
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

		int subtract(int valueOne, int valueTwo) {
			return valueOne - valueTwo;
		}

		ConnectFourBoardGame play(Disc disc, int columnNumber) {
			getColumns().findByColumnNumber(columnNumber).play(disc);
			return this;
		}

		ConnectFourBoardGame play(Disc disc, int rowIndex, int columnIndex) {
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
	static class Column {

		static final String COLUMN_TO_STRING = "Column %d";

		static int asColumnIndex(int columnNumber) {
			return columnNumber - 1;
		}

		static int asColumnNumber(int columnIndex) {
			return columnIndex + 1;
		}

		static Column from(ConnectFourBoardGame boardGame, int columnIndex) {
			return new Column(boardGame, columnIndex);
		}

		private final int index;

		private int row = ConnectFourBoardGame.ROWS;

		private final ConnectFourBoardGame boardGame;

		Column(ConnectFourBoardGame boardGame, int columnIndex) {

			Assert.notNull(boardGame, "ConnectFourBoardGame is required");
			Assert.isTrue(columnIndex > -1, "Column Index [%d] must be greater than equal to 0");

			this.boardGame = boardGame;
			this.index = columnIndex;
		}

		int getNumber() {
			return asColumnNumber(getIndex());
		}

		boolean isPlayable() {
			return getRow() > 0;
		}

		int getRowIndex() {
			return rowIndex(getRow());
		}

		Column play(Disc disc) {
			int rowIndex = nextRowIndex();
			int columnIndex = getIndex();
			getBoardGame().play(disc, rowIndex, columnIndex);
			return this;
		}

		private int nextRow() {
			return this.row--;
		}

		private int nextRowIndex() {
			return rowIndex(nextRow());
		}

		private int rowIndex(int row) {
			return row - 1;
		}

		@Override
		public String toString() {
			return COLUMN_TO_STRING.formatted(getNumber());
		}
	}

	interface Columns extends Iterable<Column> {

		static Columns from(ConnectFourBoardGame boardGame) {

			Assert.notNull(boardGame, "ConnectFourBoardGame is required");

			List<Column> columns = IntStream.range(0, ConnectFourBoardGame.COLUMNS)
				.mapToObj(columnIndex -> Column.from(boardGame, columnIndex))
				.toList();

			return Columns.of(columns);
		}

		static Columns of(Column... columns) {
			return of(Arrays.asList(columns));
		}

		static Columns of(Iterable<Column> columns) {
			return columns::iterator;
		}

		default Optional<Column> findBy(Predicate<Column> predicate) {
			return stream().filter(predicate).findFirst();
		}

		default Column findByColumnIndex(int columnIndex) {
			return findBy(column -> column.getIndex() == columnIndex)
				.orElseThrow(() -> newIllegalStateException("Column with index [%d] not found"
					.formatted(columnIndex)));
		}

		default Column findByColumnNumber(int columnNumber) {
			return findBy(column -> column.getNumber() == columnNumber)
				.orElseThrow(() -> newIllegalStateException("Column with number [%d] not found"
					.formatted(columnNumber)));
		}

		default Columns findPlayableColumns() {
			List<Column> playableColumns = stream().filter(Column::isPlayable).toList();
			return of(playableColumns);
		}

		default int size() {
			return Long.valueOf(stream().count()).intValue();
		}

		default Stream<Column> stream() {
			return StreamUtils.stream(this);
		}
	}

	@Getter
	enum Disc {

		RED("X"), GOLD("O");

		private final String symbol;

		Disc(String symbol) {
			this.symbol = StringUtils.requireText(symbol, "Symbol is required");
		}

		static boolean exists(Disc disc) {
			return disc != null;
		}
	}
}
