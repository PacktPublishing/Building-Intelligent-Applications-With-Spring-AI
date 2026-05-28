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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.DatabindException;
import com.packt.spring.ai.examples.connect4.model.ConnectFourBoardGame;
import com.packt.spring.ai.examples.connect4.model.Disc;
import com.packt.spring.ai.examples.connect4.model.Play;
import com.packt.spring.ai.examples.connect4.model.Player;
import com.packt.spring.ai.examples.connect4.model.PlayerAction;
import com.packt.spring.ai.examples.connect4.model.Players;
import com.packt.spring.ai.examples.connect4.support.ConnectFourException;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.util.AbstractTimer;

import org.cp.elements.util.MapBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} using Spring AI with Google Gemini vs. OpenAI in a game of Connect 4.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Profile
 * @see AbstractConnectFourApplication
 * @see AiProvider
 * @see ConnectFourBoardGame
 * @see PlayerAction
 * @see Players
 * @see Player
 * @see Play
 * @see <a href="https://en.wikipedia.org/wiki/Connect_Four">connect4</a>
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(ConnectFourApplication.CONNECT_FOUR_PROFILE)
@Slf4j(topic = "connect-four-app")
@SuppressWarnings("unused")
public class ConnectFourApplication extends AbstractConnectFourApplication {

	private static final boolean LOG_EXPLANATION = true;
	private static final boolean MOCK_AI_ENABLED = false;

	private static final int MAX_RETRY_ATTEMPTS = 2;

	private static final String TRIED_COLUMN_PROMPT_TEMPLATE_ARGUMENT = "triedColumn";

	static final String CONNECT_FOUR_PROFILE = "connect4";

	private static final String RETRY_PROMPT_TEMPLATE = """
		"{triedColumn}" is not an available column! You can only play 1 of the letters in: {availableColumns}. Try again!
	""";

	// System & User Prompts prompting the AI models on how to play Connect4 and instructing them to play.
	private static final String SYSTEM_PROMPT_TEMPLATE = """
		You are a player in the 2 player board game Connect4. The game board is a 2 dimensional grid with 6 rows
		and 7 columns. A player chooses a column and plays either the letter "X" or the letter "O". To win, a player
		must be the first one to play the same letter in 4 adjacent cells of the grid, either in the same row,
		the same column, or diagonally. Play continues until a player wins or there are no more available moves.
	""";

	private static final String USER_PROMPT_TEMPLATE = """
		You are playing letter "{playerDisc}". The current state of the game board is "{gameBoard}". Think carefully
		and strategically about your next move. Minimize the number of moves needed to connect 4 and beat your opponent.
		If you have an opportunity to win, then you must make a move to win. If your opponent has an opportunity to win,
		then you should block your opponent and prevent them from winning. Select 1 of the available columns represented
		as a letter in: {availableColumns}. What is your move? Explain.
	""";

	private static final List<SpringAiProvider> AI_PROVIDERS = List.of(
		SpringAiProvider.OLLAMA,
		SpringAiProvider.OPEN_AI,
		SpringAiProvider.VERTEX_AI_GEMINI,
		SpringAiProvider.MISTRAL_AI
	);

	public static void main(String[] args) {
		runSpringApplication(ConnectFourApplication.class, useProfiles(CONNECT_FOUR_PROFILE), args);
	}

	/**
	 * {@link SpringBootConfiguration} used to configure the Connect4 board game.
	 *
	 * @see org.springframework.ai.chat.client.ChatClient
	 * @see org.springframework.ai.chat.model.ChatModel
	 * @see org.springframework.boot.SpringBootConfiguration
	 * @see org.springframework.context.annotation.Bean
	 * @see ConnectFourBoardGame
	 * @see CompositeChatModel
	 * @see EnableChatClient
	 */
	@SpringBootConfiguration
	@EnableChatClient
	static class ConnectFourConfiguration {

		@Bean
		ConnectFourBoardGame boardGame() {
			return new ConnectFourBoardGame();
		}

		@Bean
		RetryTemplate retryTemplate() {

			RetryPolicy retryPolicy = new SimpleRetryPolicy(MAX_RETRY_ATTEMPTS, Map.of(
				ConnectFourException.class, true,
				DatabindException.class, true,
				IndexOutOfBoundsException.class, true
			), true);

			RetryTemplate retryTemplate = new RetryTemplate();

			retryTemplate.setRetryPolicy(retryPolicy);

			return retryTemplate;
		}
	}

	/**
	 * Main {@link ApplicationRunner program runner} used to play Connect 4.
	 *
	 * @param environment Spring {@link Environment} used to access environment-specific application configuration.
	 * @param chatClient {@link ChatClient} used to send chat requests to configured AI models.
	 * @param chatModel {@link CompositeChatModel} encapsulating access to all available, configured
	 *  declared on the application classpath.
	 * @param boardGame {@link ConnectFourBoardGame} used to manage game state.
	 * @return a new {@link ApplicationRunner} to initiate the board game.
	 * @see org.springframework.ai.chat.client.ChatClient
	 * @see org.springframework.boot.ApplicationRunner
	 * @see org.springframework.core.env.Environment
	 * @see ConnectFourBoardGame
	 * @see CompositeChatModel
	 */
	@Bean
	ApplicationRunner playGame(Environment environment, ChatClient chatClient, CompositeChatModel chatModel,
			ConnectFourBoardGame boardGame, RetryTemplate retryTemplate) {

		return args -> {

			print("%n%nWelcome to Connect4!%n%n");

			Scanner input = new Scanner(System.in);
			Players players = selectPlayers(input);
			Player currentPlayer = players.startingPlayer(SECURE_RANDOM, chatModel);

			while (boardGame.isPlayable()) {

				String model = resolveModel(environment, currentPlayer);

				Player activePlayer = currentPlayer;

				Map<String, Object> promptTemplateArguments =
					resolvePromptTemplateArguments(boardGame, currentPlayer);

				AtomicReference<String> playerMove = new AtomicReference<>(null);
				AtomicReference<Duration> totalPlayTime = new AtomicReference<>(Duration.ZERO);

				logCurrentPlayer(currentPlayer);
				logModelInput(model, promptTemplateArguments, boardGame);

				retry(retryTemplate, retryContext -> {

					if (retryContext.getRetryCount() > 0) {
						logWarn("Player [{}] incorrectly attempted to play [{}]; Retry count [{}]", () -> new Object[] {
							activePlayer.getName(), playerMove.get(), retryContext.getRetryCount()
						});
						promptTemplateArguments.put(TRIED_COLUMN_PROMPT_TEMPLATE_ARGUMENT, playerMove.get());
					}

					AbstractTimer<?, Play> playTimer =
						AbstractTimer.time(() -> promptModel(model, promptTemplateArguments, chatClient));

					Play play = playTimer.run();

					Duration playTime = totalPlayTime.updateAndGet(it -> it.plus(playTimer.getTime()));

					PlayerAction playerAction = PlayerAction.by(activePlayer).played(play).in(playTime);

					playerMove.set(playerAction.move());
					logExplanation(playerAction);

					playSafely(gameBoard -> {
						gameBoard.play(playerAction);
						gameBoard.printGameBoard();
					}).accept(boardGame);

					return playerAction;
				});

				if (boardGame.isPlayable()) {
					currentPlayer = players.switchPlayer(chatModel);
					print("Press <enter> to continue next play ");
					waitOnUserInput(input);
				}
			}

			endGame(boardGame, players);
		};
	}

	private Players selectPlayers(Scanner input) {

		AtomicInteger count = new AtomicInteger(1);

		AI_PROVIDERS.stream()
			.map(provider -> "%d. %s%n".formatted(count.getAndIncrement(), provider.getName()))
			.forEach(ConnectFourApplication::print);

		print("%nSelect player one: ");

		Player playerOne = selectPlayer(input, Disc.GOLD);

		print("Select player two: ");

		Player playerTwo = selectPlayer(input, Disc.RED);

		print("Player 1 [%s] is playing [%s]%n", playerOne.getName(), playerOne.disc().toColoredString());
		print("Player 2 [%s] is playing [%s]%n%n", playerTwo.getName(), playerTwo.disc().toColoredString());

		return Players.of(playerOne, playerTwo);
	}

	private Player selectPlayer(Scanner input, Disc disc) {
		int selection = input.nextInt();
		int providerIndex = Math.max(Math.abs(selection) - 1, 0) % AI_PROVIDERS.size();
		AiProvider provider = AI_PROVIDERS.get(providerIndex);
		return Player.from(provider).playing(disc);
	}

	private Map<String, Object> resolvePromptTemplateArguments(ConnectFourBoardGame boardGame, Player player) {

		return MapBuilder.<String, Object>newHashMap()
			.put("gameBoard", "\n\n%s\n\n".formatted(boardGame.getGameBoardStateAsGrid()))
			.put("playerDisc", player.disc().getSymbol())
			.put("availableColumns", Arrays.toString(boardGame.getPlayableColumnsAsLetter()))
			.build();
	}

	private Play promptModel(String model, Map<String, Object> promptTemplateArguments, ChatClient chatClient) {

		return MOCK_AI_ENABLED
			? promptMockModel(model, promptTemplateArguments, chatClient)
			: promptRealModel(model, promptTemplateArguments, chatClient);
	}

	@Override
	String systemPromptTemplate() {
		return SYSTEM_PROMPT_TEMPLATE;
	}

	@Override
	String userPromptTemplate(Map<String, Object> promptTemplateArguments) {

		return promptTemplateArguments.containsKey(TRIED_COLUMN_PROMPT_TEMPLATE_ARGUMENT)
			? RETRY_PROMPT_TEMPLATE.concat(System.lineSeparator()).concat(USER_PROMPT_TEMPLATE)
			: USER_PROMPT_TEMPLATE;
	}

	private void logCurrentPlayer(Player currentPlayer) {
		print("Current player is [%s] playing [%s]%n%n",
			currentPlayer.getName(), currentPlayer.disc().toColoredString());
	}

	private void logExplanation(PlayerAction playerAction) {

		if (LOG_EXPLANATION) {
			logGamePlay("AI model move [{}]", playerAction.move());
			logGamePlay("AI model explanation [{}]", playerAction.reason());
			logGamePlay("AI model decision duration {} ms", playerAction.time().toMillis());
		}
	}

	private void logGamePlay(String message, Object... arguments) {
		log.info(message, arguments);
	}

	private void logModelInput(String model, Map<String, Object> promptTemplateArguments,
			ConnectFourBoardGame boardGame) {

		logDebug("Model [{}]", model);
		logDebug("Prompt Arguments [{}]", promptTemplateArguments);
		logDebug("Available Columns {}", Arrays.toString(boardGame.getPlayableColumnsAsLetter()));
	}

	private Consumer<ConnectFourBoardGame> playSafely(Consumer<ConnectFourBoardGame> boardGameConsumer) {

		return boardGame -> {
			try {
				boardGameConsumer.accept(boardGame);
			}
			catch (RuntimeException cause) {
				logWarn("Available Columns {}", Arrays.toString(boardGame.getPlayableColumnsAsLetter()));
				logWarn("Connect4 Game Board State [{}]", boardGame.getGameBoardStateAsGrid());
				throw ConnectFourException.because("AI model fumbled the ball", cause);
			}
		};
	}

	private PlayerAction retry(RetryTemplate retryTemplate,
			RetryCallback<PlayerAction, ConnectFourException> retryCallback) {

		return retryTemplate.execute(retryCallback);
	}

	private void waitOnUserInput(Scanner input) {
		input.nextLine();
	}

	private void endGame(ConnectFourBoardGame boardGame, Players players) {

		Disc winningDisc = boardGame.getWinner();

		if (winningDisc != null) {
			Player winningPlayer = players.findByDisc(winningDisc);
			print("[%s] playing [%s] wins!%n%n", winningPlayer.getName(), winningDisc);
		}
		else {
			print("No Winner!");
		}
	}
}
