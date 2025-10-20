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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.packt.spring.ai.examples.connect4.model.ConnectFourBoardGame;
import com.packt.spring.ai.examples.connect4.model.Disc;
import com.packt.spring.ai.examples.connect4.model.Play;
import com.packt.spring.ai.examples.connect4.model.Player;
import com.packt.spring.ai.examples.connect4.model.PlayerAction;
import com.packt.spring.ai.examples.connect4.model.Players;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * {@link SpringBootApplication} using Spring AI with Google Gemini vs. OpenAI in a game of Connect 4.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.ai.chat.client.ChatClient
 * @see ConnectFourBoardGame
 * @see CompositeChatModel
 * @see EnableChatClient
 * @see AiProvider
 * @see PlayerAction
 * @see Players
 * @see Player
 * @see Play
 * @see <a href="https://en.wikipedia.org/wiki/Connect_Four">connect4</a>
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(ConnectFourApplication.CONNECT_FOUR_PROFILE)
@SuppressWarnings("unused")
public class ConnectFourApplication extends AbstractConnectFourApplication {

	private static final boolean LOG_EXPLANATION = true;
	private static final boolean MOCK_AI_ENABLED = false;

	static final String CONNECT_FOUR_PROFILE = "connect4";

	private static final String SYSTEM_PROMPT_TEMPLATE = """
		You are a player in the 2 player board game Connect4. The game board is a 2 dimensional grid with 6 rows
		and 7 columns. A player chooses a column and plays either the letter "X" or the letter "O". To win, a player
		must be the first to play the same letter in 4 adjacent cells of the grid, either in the same row,
		the same column, or diagonally. Play continues until a player wins or there are no more available moves.
	""";

	private static final String USER_PROMPT_TEMPLATE = """
		You are playing letter "{playerDisc}". The current state of the game board is "{gameBoard}". Think carefully
		and strategically about your next move. Minimize the number of moves needed to connect 4 and beat your opponent.
		Select 1 of the available columns represented as a letter in {availableColumns}. What is your move? Explain.
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

			print("%n%nWelcome to Connect4!%n%n");

			Scanner input = new Scanner(System.in);

			Players players = selectPlayers(input);
			Player currentPlayer = players.startingPlayer(SECURE_RANDOM, chatModel);

			while (boardGame.isPlayable()) {

				print("Current player is [%s]%n%n", currentPlayer.getName());

				Map<String, Object> promptTemplateArguments = resolvePromptTemplateArguments(boardGame, currentPlayer);
				String model = resolveModel(environment, currentPlayer);

				logDebug("Prompt Arguments [{}]; Model [{}]", promptTemplateArguments, model);
				logInfo("Available Columns {}", Arrays.toString(boardGame.getPlayableColumnsAsLetter()));

				Play play = promptModel(model, promptTemplateArguments, chatClient);
				PlayerAction playerAction = PlayerAction.by(currentPlayer).played(play);

				logExplanation(playerAction);

				boardGame.play(playerAction);
				boardGame.printGameBoard();

				currentPlayer = players.switchPlayer(chatModel);
				print("Press <enter> to continue to next play ");
				waitForUserInput(input);
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

		print("Player 1 [%s] is playing [%s]%n", playerOne.getName(), playerOne.disc());
		print("Player 2 [%s] is playing [%s]%n%n", playerTwo.getName(), playerTwo.disc());

		return Players.of(playerOne, playerTwo);
	}

	private Player selectPlayer(Scanner input, Disc disc) {
		int selection = input.nextInt();
		int providerIndex = selection - 1;
		AiProvider provider = AI_PROVIDERS.get(providerIndex);
		return Player.from(provider).playing(disc);
	}

	private Map<String, Object> resolvePromptTemplateArguments(ConnectFourBoardGame boardGame, Player player) {
		return Map.of(
			"gameBoard", "\n\n%s\n\n".formatted(boardGame.getGameBoardStateAsGrid()),
			"playerDisc", player.disc().getSymbol(),
			"availableColumns", Arrays.toString(boardGame.getPlayableColumnsAsLetter())
		);
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
	String userPromptTemplate() {
		return USER_PROMPT_TEMPLATE;
	}

	private void logExplanation(PlayerAction playerAction) {
		if (LOG_EXPLANATION) {
			logInfo("AI model explanation [{}]", playerAction.reason());
		}
	}

	private void waitForUserInput(Scanner input) {
		input.nextLine();
	}

	private void endGame(ConnectFourBoardGame boardGame, Players players) {

		Disc winningDisc = boardGame.getWinner();

		if (winningDisc != null) {
			Player winningPlayer = players.findByDisc(winningDisc);
			print("[%s] playing [%s] wins!", winningPlayer.getName(), winningDisc);
		}
		else {
			print("No Winner!");
		}
	}
}
