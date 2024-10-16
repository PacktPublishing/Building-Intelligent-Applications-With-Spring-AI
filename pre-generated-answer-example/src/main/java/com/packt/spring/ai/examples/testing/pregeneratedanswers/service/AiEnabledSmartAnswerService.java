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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.service;

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.AnswerNotFoundException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Spring {@link Service} used to answer how-to questions using AI.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.HowTo
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.repo.HowToRepository
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.prompt.Prompt
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Primary
@Service
@Getter(AccessLevel.PROTECTED)
@Profile("ai-enabled-answers")
@SuppressWarnings("unused")
public class AiEnabledSmartAnswerService extends SmartAnswerService {

	private final ChatClient chatClient;

	public AiEnabledSmartAnswerService(ChatClient chatClient, HowToRepository repository, VectorStore vectorStore) {
		super(repository, vectorStore);
		this.chatClient = chatClient;
	}

	@Override
	public Answer answer(Question question) {

		try {
			return super.answer(question);
		}
		catch (AnswerNotFoundException tryAgain) {
			return answerWithAi(question);
		}
	}

	protected Answer answerWithAi(Question question) {

		Answer answer = promptAi(question);
		Question answeredQuestion = Question.copy(question).answered(answer).build();

		recordHowTo(answeredQuestion);

		return answer;
	}

	protected Answer promptAi(Question question) {

		Prompt prompt = new Prompt(question.get());
		String answer = getChatClient().prompt(prompt).call().content();

		return Answer.from(answer);
	}

	@SuppressWarnings("all")
	protected HowTo recordHowTo(Question question) {
		HowTo howTo = HowTo.from(store(question), question.answer());
		return save(howTo);
	}
}
