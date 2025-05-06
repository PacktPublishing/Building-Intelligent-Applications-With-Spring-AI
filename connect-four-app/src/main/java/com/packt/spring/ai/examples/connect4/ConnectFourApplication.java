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

import io.codeprimate.extensions.spring.ai.chat.model.CompositeChatModel;
import io.codeprimate.extensions.spring.ai.config.EnableChatClient;
import io.codeprimate.extensions.spring.ai.provider.AiProvider;
import io.codeprimate.extensions.spring.ai.provider.support.SpringAiProvider;
import io.codeprimate.extensions.util.Utils;

import org.cp.elements.lang.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
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

	private static final boolean MOCKS_ENABLED = false;

	private static final String SYSTEM_PROMPT_TEMPLATE = """
		You are a player in the 2-player board game Connect 4. The game board is a 2 dimensional grid with 6 rows
		and 7 columns. A player chooses a column then plays an 'X' or an 'O'. To win, a player must successfully play
		the same letter in 4 adjacent cells of the grid, either in the same row, the same column, or diagonally. Play
		continues until a player wins or there are no more available moves.
	""";

	private static final String USER_PROMPT_TEMPLATE = """
		The current state of the game board is {gameBoard}. You are "{playerColor}". Play from 1 of the available
		positions represented as a letter {availableColumns}. Respond with only 1 of the letters. What is your move?
	""";

	private static final SpringAiProvider PLAYER_ONE = SpringAiProvider.OPEN_AI;
	private static final SpringAiProvider PLAYER_TWO = SpringAiProvider.VERTEX_AI_GEMINI;

	private static final SecureRandom SECURE_RANDOM =
		new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes());

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

			SpringAiProvider currentPlayer = PLAYER_ONE;

			Scanner input = new Scanner(System.in);

			while (boardGame.isPlayable()) {

				Disc currentPlayerDisc = PLAYER_TO_DISC.get(currentPlayer);

				Map<String, Object> promptTemplateArguments = Map.of(
					"gameBoard", "\n\n%s\n\n".formatted(boardGame.getGameBoardStateAsGrid()),
					"playerColor", currentPlayerDisc.name(),
					"availableColumns", Arrays.toString(boardGame.getPlayableColumnsAsLetter())
				);

				logDebug("Prompt Arguments [{}]", promptTemplateArguments);

				String model = resolveModel(environment, currentPlayer);

				//String response = promptAiModel(chatClient, promptTemplateArguments, model);
				String response = promptMockAiModel(chatClient, promptTemplateArguments, model);

				logDebug("AI model response [{}]", response);

				RowColumn rowColumn = RowColumn.fromColumnLetter(response);

				boardGame.play(currentPlayerDisc, rowColumn);
				boardGame.printGameBoard();

				print("%nHit <enter> to continue to next play ");
				waitForUserInput(input);
				currentPlayer = switchPlayer(currentPlayer, chatModel);
			}

			endGame(boardGame, PLAYER_TO_DISC);
		};
	}

	private String promptAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {
		return MOCKS_ENABLED ? promptMockAiModel(chatClient, promptTemplateArguments, model)
			: promptRealAiModel(chatClient, promptTemplateArguments, model);
	}

	@SuppressWarnings("all")
	private String promptMockAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {

		String availableColumns = String.valueOf(promptTemplateArguments.get("availableColumns"));
		String letters = StringUtils.getLetters(availableColumns);

		int index = SECURE_RANDOM.nextInt(letters.length());

		String letter = String.valueOf(letters.charAt(index));

		return letter;
	}

	private String promptRealAiModel(ChatClient chatClient, Map<String, Object> promptTemplateArguments, String model) {

		ChatResponse chatResponse = chatClient.prompt()
			.system(SYSTEM_PROMPT_TEMPLATE)
			.user(promptUserSpec -> promptUserSpec.text(USER_PROMPT_TEMPLATE).params(promptTemplateArguments))
			.options(Utils.buildChatOptions(model))
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
