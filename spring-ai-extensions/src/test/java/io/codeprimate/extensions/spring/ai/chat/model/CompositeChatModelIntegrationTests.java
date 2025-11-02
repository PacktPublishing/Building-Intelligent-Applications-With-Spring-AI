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
package io.codeprimate.extensions.spring.ai.chat.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.model.ChatModel;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests for {@link CompositeChatModel}.
 *
 * @author John Blum
 * @see CompositeChatModel
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mockito
 * @see MultithreadedTestCase
 * @see TestFramework
 * @since 0.1.0
 */
public class CompositeChatModelIntegrationTests {

	@Test
	void concurrentUse() throws Throwable {
		TestFramework.runOnce(new ConcurrentChatModelMultithreadedTestCase());
	}

	@Getter(AccessLevel.PROTECTED)
	@SuppressWarnings("unused")
	public static class ConcurrentChatModelMultithreadedTestCase extends MultithreadedTestCase {

		private final ChatModel mockChatModelZero = mock(ChatModel.class, "MockChatModelZero");
		private final ChatModel mockChatModelOne = mock(ChatModel.class, "MockChatModelOne");
		private final ChatModel mockChatModelTwo = mock(ChatModel.class, "MockChatModelTwo");

		private final CompositeChatModel chatModel;

		public ConcurrentChatModelMultithreadedTestCase() {

			this.chatModel = CompositeChatModel.of(this.mockChatModelZero, this.mockChatModelOne, this.mockChatModelTwo);
			this.chatModel.use(this.mockChatModelZero);
		}

		@Override
		public void initialize() {

			assertThat(getChatModel()).isNotNull();
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelZero());
		}

		public void thread1() {

			Thread.currentThread().setName("AI model request 1");

			assertTick(0);
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelZero());

			waitForTick(2);

			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelZero());
			assertThat(getChatModel().use(getMockChatModelOne())).isSameAs(getChatModel());
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelOne());
		}

		public void thread2() {

			Thread.currentThread().setName("AI model request 2");

			assertTick(0);
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelZero());

			waitForTick(1);
			assertTick(1);

			assertThat(getChatModel().use(getMockChatModelTwo())).isSameAs(getChatModel());
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelTwo());

			waitForTick(3);

			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelTwo());
		}

		@Override
		public void finish() {
			assertThat(getChatModel().getCurrentChatModel()).isEqualTo(getMockChatModelZero());
			verifyNoInteractions(getMockChatModelZero(), getMockChatModelOne(), getMockChatModelTwo());
		}
	}
}
