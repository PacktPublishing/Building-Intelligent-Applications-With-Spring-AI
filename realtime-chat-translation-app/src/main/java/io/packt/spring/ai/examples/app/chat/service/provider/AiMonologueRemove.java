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
package io.packt.spring.ai.examples.app.chat.service.provider;

import io.packt.spring.ai.examples.app.chat.model.TextMessage;
import io.packt.spring.ai.examples.app.chat.service.MonologueRemover;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link MonologueRemover} implementation used to remove monologue in an AI's generated chat response.
 *
 * @author John Blum
 * @see MonologueRemover
 * @see Service
 * @since 0.1.0
 */
@Service
@SuppressWarnings("unused")
public class AiMonologueRemove implements MonologueRemover {

	@Override
	public TextMessage removeMonologue(TextMessage message) {

		Assert.notNull(message, "TextMessage is required");

		String text = message.getText();

		if (StringUtils.hasText(text)) {
			int leftBracketIndex = text.indexOf("[");
			if (leftBracketIndex > -1) {
				int rightRightIndex = text.lastIndexOf("]");
				rightRightIndex = rightRightIndex > -1 ? rightRightIndex : text.length();
				leftBracketIndex++;
				String resolvedText = text.substring(leftBracketIndex, rightRightIndex);
				return TextMessage.from(resolvedText);
			}
		}

		return message;
	}
}
