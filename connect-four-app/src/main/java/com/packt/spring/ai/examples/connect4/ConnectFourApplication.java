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
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.stream.StreamUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link SpringBootApplication} using Spring AI with Google Gemini vs. OpenAI in a game of Connect 4.
 *
 * @author John Blum
 * @see io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel
 * @see io.codeprimate.extensions.spring.ai.config.EnableChatClient
 * @see io.codeprimate.extensions.spring.ai.provider.AiProvider
 * @see io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see <a href="https://en.wikipedia.org/wiki/Connect_Four">connect4</a>
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(ConnectFourApplication.CONNECT_FOUR_PROFILE)
@SuppressWarnings("unused")
public class ConnectFourApplication extends AbstractSpringBootApplication {

	private static final int CONNECT_FOUR = 4;

	private static final BiFunction<Integer, Integer, Integer> BI_FUNCTION_IDENTITY =
		(argumentOne, argumentTwo) -> argumentOne;

	private static final SpringAiProvider PLAYER_ONE = SpringAiProvider.OPEN_AI;
	private static final SpringAiProvider PLAYER_TWO = SpringAiProvider.VERTEX_AI_GEMINI;

	protected static final String CONNECT_FOUR_PROFILE = "connect4";

	private static final String SYSTEM_PROMPT_TEMPLATE = """
		You are a player in the 2-player game Connect 4. The game board is 6 rows by 7 columns. Let R1 represent row 1.
		Let R2 represent row 2 and so up to R6 representing row 6. Let C1 represent column 1. Let C2 represent column 2
		and so on up to C7 representing column 7. Your objective is to connect 4 adjacent chips of the same color
		horizontally in a single row, or vertically in a single column, or diagonally by row and column. For example,
		you can connect 4 chips in a row with [(R2,C2),(R2,C3),(R2,C4),(R2,C5)]. You can also connect 4 chips by column,
		for example: [(R1,C3),(R2,C3),(R3,C3),(R4,C4)]. And, you can connect 4 chips diagonally, for example:
		[(R1,C2),(R2,C3),(R3,C4),(R4,C5)]. If you connect 4 adjacent chips, you win! You must also be careful to prevent
		your opponent from connecting 4. The first player to connect 4 adjacent chips wins! A position on the game board
		maybe empty, for example (R1,C1)=empty, or contain a chip, for example (R2,C4)=GOLD. Chip colors are 'GOLD'
		and 'RED'. You will play until you or your opponent connects 4, or there are no more available moves.
	""";

	private static final String USER_PROMPT_TEMPLATE = """
        The current state of the game board is {gameBoard}. Your chip color is {playerColor}. Your possible moves
        by column are {availableMoves}. Try to connect 4 or block your opponent. You have a single move. What is
        your move?
    """;

	public static void main(String[] args) {
		runSpringApplication(ConnectFourApplication.class, useProfiles(CONNECT_FOUR_PROFILE), args);
	}

	@SpringBootConfiguration
	@EnableChatClient
	static class ConnectFourConfiguration {

		@Bean
		ConnectFourBoardGame boardGame() {
			return new ConnectFourBoardGame();
		}
	}

	@Bean
	ApplicationRunner playGame(Environment environment, ChatClient chatClient, CompositeChatModel chatModel,
			ConnectFourBoardGame boardGame) {

		return args -> {

			SpringAiProvider currentPlayer = PLAYER_ONE;

			Map<AiProvider, Disc> playerDisc = Map.of(
				PLAYER_ONE, Disc.RED,
				PLAYER_TWO, Disc.GOLD
			);

			Scanner input = new Scanner(System.in);

			while (boardGame.isPlayable()) {

				Disc currentPlayerDisc = playerDisc.get(currentPlayer);

				Map<String, Object> promptTemplateArguments = Map.of(
					"gameBoard", Arrays.toString(boardGame.getGameBoardStateBySymbol()),
					"playerColor", currentPlayerDisc.name(),
					"availableMoves", Arrays.toString(boardGame.getPlayableColumnsBySymbol())
				);

				String model = resolveModel(environment, currentPlayer);

				String response = promptAiModel(chatClient, promptTemplateArguments, model);

				RowColumn rowColumn = RowColumn.parse(response);

				boardGame.play(currentPlayerDisc, rowColumn);
				boardGame.printGameBoard();

				print("%nHit <enter> to continue to next play");
				waitForUserInput(input);
				currentPlayer = switchPlayer(currentPlayer, chatModel);
			}

			endGame(boardGame, playerDisc);
		};
	}

	private String promptAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {

		ChatResponse chatResponse = chatClient.prompt()
			.system(SYSTEM_PROMPT_TEMPLATE)
			.user(promptUserSpec -> promptUserSpec.text(USER_PROMPT_TEMPLATE).params(promptTemplateArguments))
			.options(ChatOptionsBuilder.builder().withModel(model).build())
			.call()
			.chatResponse();

		return Utils.generatedContent(chatResponse);
	}

	private String resolveModel(Environment environment, SpringAiProvider aiProvider) {

		String propertyName = SpringAiProvider.SPRING_AI_CHAT_OPTIONS_MODEL_PROPERTY_TEMPLATE
			.formatted(aiProvider.getPropertyName());

		return environment.getProperty(propertyName);
	}

	private SpringAiProvider switchPlayer(AiProvider currentPlayer, CompositeChatModel chatModel) {
		SpringAiProvider nextPlayer = currentPlayer.equals(PLAYER_ONE) ? PLAYER_TWO : PLAYER_ONE;
		getLogger().info("Using AI provider model [{}]", chatModel.use(nextPlayer).getCurrentChatModel());
		return nextPlayer;
	}

	private void waitForUserInput(Scanner input) {
		input.nextLine();
	}

	private void endGame(ConnectFourBoardGame boardGame, Map<AiProvider, Disc> playerDisc) {

		Disc winner = boardGame.getWinner();

		if (winner != null) {

			AiProvider winningAiProvider = playerDisc.entrySet().stream()
				.filter(entry -> entry.getValue().equals(winner))
				.map(Map.Entry::getKey).findFirst()
				.orElseThrow(() -> new IllegalStateException("No AI provider mapped to Disc [%s]"
					.formatted(winner)));

			print("[%s] as [%s] wins!", winningAiProvider.getName(), winner.name());
		}
		else {
			print("No Winner!");
		}
	}

	@Getter(AccessLevel.PROTECTED)
	static class ConnectFourBoardGame {

		private static final int ROWS = 6;
		private static final int COLUMNS = 7;

		private static final String COLUMN_SYMBOL = "C%d";
		private static final String ROW_SYMBOL = "R%d";
		private static final String ROW_COLUMN_SYMBOL = "("+ROW_SYMBOL+","+COLUMN_SYMBOL+")";
		private static final String ROW_COLUMN_VALUE = ROW_COLUMN_SYMBOL+"=%s";

		private final Columns columns;

		private final Disc[][] gameBoard = new Disc[ROWS][COLUMNS];

		ConnectFourBoardGame() {

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				Arrays.fill(this.gameBoard[rowIndex], null);
			}

			// Not Thread-safe; this reference escapes!
			this.columns = Columns.from(this);
		}

		String[] getGameBoardStateBySymbol() {

			String[] rowColumnValues = new String[ROWS * COLUMNS];

			for (int rowIndex = 0; rowIndex < ROWS; rowIndex++) {
				for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
					int index = rowIndex * COLUMNS + columnIndex;
					Disc disc = this.gameBoard[rowIndex][columnIndex];
					String value = disc != null ? disc.name() : "empty";
					rowColumnValues[index] = ROW_COLUMN_VALUE
						.formatted(RowColumn.asRowNumber(rowIndex), RowColumn.asColumnNumber(columnIndex), value);
				}
			}

			return rowColumnValues;
		}

		Columns getPlayableColumns() {
			return getColumns().findPlayableColumns();
		}

		String[] getPlayableColumnsBySymbol() {

			return getPlayableColumns().stream()
				.map(Column::getNumber)
				.map(COLUMN_SYMBOL::formatted)
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
			// return columnIndex >= 2;
			return RowColumn.asColumnNumber(columnIndex) - CONNECT_FOUR >= 0;
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

		private int subtract(int valueOne, int valueTwo) {
			return valueOne - valueTwo;
		}

		ConnectFourBoardGame play(Disc disc, int columnNumber) {
			getColumns().findByColumnNumber(columnNumber).play(disc);
			return this;
		}

		ConnectFourBoardGame play(Disc disc, RowColumn rowColumn) {
			getColumns().findByColumnNumber(rowColumn.columnNumber()).play(disc);
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
			return RowColumn.asColumnNumber(getIndex());
		}

		boolean isPlayable() {
			return getRow() > 0;
		}

		int getRowIndex() {
			return RowColumn.asRowIndex(getRow());
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
			return RowColumn.asRowIndex(nextRow());
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

		static RowColumn parse(String value) {

			Assert.hasText(value, () -> "Value [%s] to parse as a row/column is required".formatted(value));

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

			throw new IllegalArgumentException("Failed to parse row/column from [%s]".formatted(value));
		}

		int getColumnIndex() {
			return asColumnIndex(columnNumber());
		}

		int getRowIndex() {
			return asRowIndex(rowNumber());
		}
	}
}
