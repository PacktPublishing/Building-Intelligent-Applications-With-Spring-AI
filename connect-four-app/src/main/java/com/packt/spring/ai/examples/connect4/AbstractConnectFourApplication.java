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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.packt.spring.ai.examples.connect4.model.Disc;
import com.packt.spring.ai.examples.connect4.model.Play;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.stream.StreamUtils;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Abstract base class encapsulating common components and functionality used to implements the Connect 4 application.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractConnectFourApplication extends AbstractSpringBootApplication {

	static final int CONNECT_FOUR = 4;

	static final String CONNECT_FOUR_PROFILE = "connect4";

	@Getter(AccessLevel.PROTECTED)
	static class ConnectFourBoardGame {

		private static final int ROWS = 6;
		private static final int COLUMNS = 7;
		private static final int GAME_BOARD_SIZE = ROWS * COLUMNS;

		private static final String COLUMN_POSITIONS = "ABCDEFG";
		private static final String COLUMN_SYMBOL = "C%d";
		private static final String ROW_SYMBOL = "R%d";
		private static final String ROW_COLUMN_SYMBOL = "("+ROW_SYMBOL+", "+COLUMN_SYMBOL+")";

		private final ConnectFourApplication.Columns columns;

		private final Disc[][] gameBoard = new Disc[ROWS][COLUMNS];

		ConnectFourBoardGame() {

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				Arrays.fill(this.gameBoard[rowIndex], null);
			}

			// Not Thread-safe; this reference escapes!
			this.columns = ConnectFourApplication.Columns.from(this);
		}

		String[] getGameBoardStateAsArray() {

			String[] rowColumnValues = new String[ROWS * COLUMNS];

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
					Disc disc = this.gameBoard[rowIndex][columnIndex];
					String name = Disc.resolveSymbol(disc, "-");
					int index = rowIndex * COLUMNS + columnIndex;
					rowColumnValues[index] = name;
				}
			}

			return rowColumnValues;
		}

		String getGameBoardStateAsGrid() {

			StringBuilder grid = new StringBuilder(GAME_BOARD_SIZE);
			int count = 0;

			for (String element : getGameBoardStateAsArray()) {
				if (count++ % COLUMNS == 0) {
					grid.append(Utils.NEW_LINE);
				}
				grid.append(element);
			}

			return grid.toString();
		}

		ConnectFourApplication.Columns getPlayableColumns() {
			return getColumns().findPlayableColumns();
		}

		String[] getPlayableColumnsAsLetter() {

			return getPlayableColumns().stream()
				.map(ConnectFourApplication.Column::getIndex)
				.map(COLUMN_POSITIONS::charAt)
				.map(String::valueOf)
				.toList()
				.toArray(String[]::new);
		}

		boolean canPlay() {
			return getPlayableColumns().isNotEmpty();
		}

		boolean isPlayable() {
			return isNoWinner() && canPlay();
		}

		boolean isWinner() {
			return getWinner() != null;
		}

		boolean isNoWinner() {
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
			return ConnectFourApplication.RowColumn.asColumnNumber(columnIndex) - CONNECT_FOUR >= 0;
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

		private Disc checkConnectFour(Disc disc,
				int rowIndex, int columnIndex,
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

		private Disc checkConnectFourDiagonally(Disc disc,
				int rowIndex, int columnIndex,
				BiFunction<Integer, Integer, Integer> columnIndexFunction) {

			return checkConnectFour(disc, rowIndex, columnIndex, Integer::sum, columnIndexFunction);
		}

		private Disc checkConnectFourDiagonallyBackward(Disc disc,
				int rowIndex, int columnIndex) {

			return checkConnectFourDiagonally(disc, rowIndex, columnIndex, this::subtract);
		}

		private Disc checkConnectFourDiagonallyForward(Disc disc,
				int rowIndex, int columnIndex) {

			return checkConnectFourDiagonally(disc, rowIndex, columnIndex, Integer::sum);
		}

		private Disc checkConnectFourForward(Disc disc,
				int rowIndex, int columnIndex) {

			return isForwardPossible(columnIndex)
				? checkConnectFour(disc, rowIndex, columnIndex, Utils.biFunctionReturnArgumentOne(), Integer::sum)
				: null;
		}

		private Disc checkConnectFourUp(Disc disc,
				int rowIndex, int columnIndex) {

			return isUpPossible(rowIndex)
				? checkConnectFour(disc, rowIndex, columnIndex, Integer::sum, Utils.biFunctionReturnArgumentOne())
				: null;
		}

		private int subtract(int valueOne, int valueTwo) {
			return valueOne - valueTwo;
		}

		ConnectFourBoardGame play(Disc disc, int columnNumber) {
			getColumns().findByColumnNumber(columnNumber).play(disc);
			return this;
		}

		ConnectFourBoardGame play(Disc disc, Play play) {
			RowColumn rowColumn = RowColumn.fromColumnLetter(play.move());
			return play(disc, rowColumn);
		}

		ConnectFourBoardGame play(Disc disc, RowColumn rowColumn) {
			return play(disc, rowColumn.columnNumber());
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
			return ConnectFourApplication.RowColumn.asColumnNumber(getIndex());
		}

		boolean isPlayable() {
			return getRow() > 0;
		}

		int getRowIndex() {
			return ConnectFourApplication.RowColumn.asRowIndex(getRow());
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
			return ConnectFourApplication.RowColumn.asRowIndex(nextRow());
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

		default boolean isEmpty() {
			return size() < 1;
		}

		default boolean isNotEmpty() {
			return !isEmpty();
		}

		default int size() {
			return Long.valueOf(stream().count()).intValue();
		}

		default Stream<Column> stream() {
			return StreamUtils.stream(this);
		}
	}

	record RowColumn(int rowNumber, int columnNumber) {

		static final String COLUMN_REGEX = "C(\\d+)";
		static final String ROW_COLUMN_REGEX = "R(\\d+),"+ COLUMN_REGEX;

		static final Pattern COLUMN_PATTERN = Pattern.compile(COLUMN_REGEX);
		static final Pattern ROW_COLUMN_PATTERN = Pattern.compile(ROW_COLUMN_REGEX);

		static int asColumnIndex(int columnNumber) {
			return columnNumber - 1;
		}

		static int asColumnNumber(int columnIndex) {
			return columnIndex + 1;
		}

		static int asRowIndex(int rowNumber) {
			return rowNumber - 1;
		}

		static int asRowNumber(int rowIndex) {
			return rowIndex + 1;
		}

		static RowColumn fromColumn(int columnNumber) {
			return new RowColumn(0, columnNumber);
		}

		static RowColumn fromColumnLetter(String letter) {
			String singleLetter = assertSingleLetter(StringUtils.getLetters(letter));
			int index = assertIndexInbounds(ConnectFourBoardGame.COLUMN_POSITIONS.indexOf(singleLetter), singleLetter);
			return RowColumn.fromColumn(RowColumn.asColumnNumber(index));
		}

		private static int assertIndexInbounds(int index, String letter) {
			Assert.isTrue(index > -1, new IndexOutOfBoundsException("Index [%d] for letter [%s] is not valid"
				.formatted(index, letter)));
			return index;
		}

		private static String assertSingleLetter(String value) {
			Assert.isTrue(isSingleLetter(value), "Expected [%s] to be a single letter", value);
			return value;
		}

		private static boolean isSingleLetter(String value) {
			return value != null && value.length() == 1 && Character.isLetter(value.charAt(0));
		}

		static RowColumn fromRow(int rowNumber) {
			return new RowColumn(rowNumber, 0);
		}

		static RowColumn parse(String value) {

			Assert.hasText(value, () -> "Value [%s] to parse as a (row, column) is required".formatted(value));

			Matcher matcher = ROW_COLUMN_PATTERN.matcher(value);

			if (matcher.find()) {
				int row = Integer.parseInt(matcher.group(1));
				int column = Integer.parseInt(matcher.group(2));
				return new RowColumn(row, column);
			}
			else {
				matcher = COLUMN_PATTERN.matcher(value);
				if (matcher.find()) {
					int column = Integer.parseInt(StringUtils.getDigits(matcher.group()));
					return new RowColumn(-1, column);
				}
			}

			throw new IllegalArgumentException("Failed to parse (row, column) from [%s]".formatted(value));
		}

		int columnIndex() {
			return asColumnIndex(columnNumber());
		}

		int rowIndex() {
			return asRowIndex(rowNumber());
		}
	}
}
