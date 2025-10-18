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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import com.packt.spring.ai.examples.connect4.model.ConnectFourBoardGame;
import com.packt.spring.ai.examples.connect4.model.Disc;
import com.packt.spring.ai.examples.connect4.model.Play;

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
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
public class ConnectFourApplication extends AbstractConnectFourApplication {

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

	private static final SpringAiProvider PLAYER_ONE = SpringAiProvider.OPEN_AI;
	private static final SpringAiProvider PLAYER_TWO = SpringAiProvider.VERTEX_AI_GEMINI;

	private static final SecureRandom SECURE_RANDOM =
		new SecureRandom(UUID.randomUUID().toString().getBytes());

	private static final Map<AiProvider, Disc> PLAYER_TO_DISC = Map.of(
		PLAYER_ONE, Disc.RED,
		PLAYER_TWO, Disc.GOLD
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
			print("Player 1 is [%s] playing [%s]%n%n", PLAYER_ONE.getName(), PLAYER_TO_DISC.get(PLAYER_ONE).getSymbol());
			print("Player 2 is [%s] playing [%s]%n%n", PLAYER_TWO.getName(), PLAYER_TO_DISC.get(PLAYER_TWO).getSymbol());

			SpringAiProvider currentPlayer = randomPlayer();

			Scanner input = new Scanner(System.in);

			while (boardGame.isPlayable()) {

				print("Current player is [%s]%n%n", currentPlayer.getName());

				Disc currentPlayerDisc = PLAYER_TO_DISC.get(currentPlayer);

				Map<String, Object> promptTemplateArguments = Map.of(
					"gameBoard", "\n\n%s\n\n".formatted(boardGame.getGameBoardStateAsGrid()),
					"playerDisc", currentPlayerDisc.getSymbol(),
					"availableColumns", Arrays.toString(boardGame.getPlayableColumnsAsLetter())
				);

				logDebug("Prompt Arguments [{}]", promptTemplateArguments);

				String model = resolveModel(environment, currentPlayer);

				Play play = promptAiModel(chatClient, promptTemplateArguments, model);

				logInfo("AI model response [{}]", play.move());

				boardGame.play(currentPlayerDisc, play);
				boardGame.printGameBoard();

				currentPlayer = switchPlayer(currentPlayer, chatModel);
				print("%Press <enter> to continue to next play ");
				waitForUserInput(input);
			}

			endGame(boardGame, PLAYER_TO_DISC);
		};
	}

	private Play promptAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {
		return MOCK_AI_ENABLED ? promptMockAiModel(chatClient, promptTemplateArguments, model)
			: promptRealAiModel(chatClient, promptTemplateArguments, model);
	}

	@SuppressWarnings("all")
	private Play promptMockAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {

		String availableColumns = String.valueOf(promptTemplateArguments.get("availableColumns"));
		String letters = StringUtils.getLetters(availableColumns);

		int index = SECURE_RANDOM.nextInt(letters.length());

		String letter = String.valueOf(letters.charAt(index));

		return Play.from(letter, "Because");
	}

	private Play promptRealAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {

		BeanOutputConverter<Play> playConverter = new BeanOutputConverter<>(Play.class);

		return chatClient.prompt()
			.system(SYSTEM_PROMPT_TEMPLATE)
			.user(promptUserSpec -> promptUserSpec.text(USER_PROMPT_TEMPLATE).params(promptTemplateArguments))
			.options(Utils.buildChatOptions(model))
			.call()
			.entity(playConverter);
	}

	private String resolveModel(Environment environment, SpringAiProvider aiProvider) {

		String propertyName = SpringAiProvider.SPRING_AI_CHAT_OPTIONS_MODEL_PROPERTY_TEMPLATE
			.formatted(aiProvider.getPropertyName());

		return environment.getProperty(propertyName);
	}

	private SpringAiProvider randomPlayer() {
		return SECURE_RANDOM.nextInt(2) == 1 ? PLAYER_ONE : PLAYER_TWO;
	}

	private SpringAiProvider switchPlayer(AiProvider currentPlayer, CompositeChatModel chatModel) {
		SpringAiProvider nextPlayer = PLAYER_ONE.equals(currentPlayer) ? PLAYER_TWO : PLAYER_ONE;
		ChatModel currentChatModel = chatModel.use(nextPlayer).getCurrentChatModel();
		getLogger().info("Using AI provider model [{}]", currentChatModel);
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
}
