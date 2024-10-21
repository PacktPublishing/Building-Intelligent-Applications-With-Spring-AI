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
package io.codeprimate.tools.spring.ai.tokens.jtokkit.api;

import java.util.List;

import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingResult;
import com.knuddels.jtokkit.api.IntArrayList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract wrapper for an existing {@link Encoding}.
 *
 * @author John Blum
 * @see com.knuddels.jtokkit.api.Encoding
 * @since 0.1.0
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class EncodingWrapper<T extends Encoding> implements Encoding {

	private final T encoding;

	@Override
	public String getName() {
		return getEncoding().getName();
	}

	@Override
	public int countTokens(String text) {
		return getEncoding().countTokens(text);
	}

	@Override
	public int countTokensOrdinary(String text) {
		return getEncoding().countTokensOrdinary(text);
	}

	@Override
	public IntArrayList encode(String text) {
		return getEncoding().encode(text);
	}

	@SuppressWarnings("unused")
	public List<String> encodeAsStringTokens(String text) {
		throw new UnsupportedOperationException("Not Implemented");
	}

	@Override
	public EncodingResult encode(String text, int maxTokens) {
		return getEncoding().encode(text, maxTokens);
	}

	@Override
	public IntArrayList encodeOrdinary(String text) {
		return getEncoding().encodeOrdinary(text);
	}

	@Override
	public EncodingResult encodeOrdinary(String text, int maxTokens) {
		return getEncoding().encodeOrdinary(text, maxTokens);
	}

	@Override
	public String decode(IntArrayList tokens) {
		return getEncoding().decode(tokens);
	}

	@Override
	public byte[] decodeBytes(IntArrayList tokens) {
		return getEncoding().decodeBytes(tokens);
	}
}
