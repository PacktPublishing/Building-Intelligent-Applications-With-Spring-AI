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
package io.codeprimate.tools.spring.ai.tokens.service.provider;

import java.util.List;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.GptBytePairEncodingWrapper;
import com.knuddels.jtokkit.api.EncodingRegistry;

import io.codeprimate.tools.spring.ai.tokens.jtokkit.api.EncodingWrapper;
import io.codeprimate.tools.spring.ai.tokens.service.TokenizerService;
import io.codeprimate.extensions.spring.ai.provider.model.ModelNotFoundException;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link TokenizerService} implementation using {@literal JTokkit} library to tokenize {@link String content}
 *  or a {@link Document}.
 *
 * @author John Blum
 * @see io.codeprimate.tools.spring.ai.tokens.service.TokenizerService
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
@Service
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class JTokkitTokenizerService implements TokenizerService {

	private final EncodingRegistry encodingRegistry = Encodings.newDefaultEncodingRegistry();

	@Override
	@SuppressWarnings("all")
	public List<String> tokenize(String content, String modelName) {

		Assert.notNull(content, "Content to tokenize is required");
		Assert.hasText(modelName, "Name of model is required");

		EncodingWrapper<?> encoding = GptBytePairEncodingWrapper.wrap(getEncodingRegistry().getEncodingForModel(modelName)
			.orElseThrow(() -> ModelNotFoundException.from(modelName)));

		List<String> tokens = encoding.encodeAsStringTokens(content);

		return tokens;
	}
}
