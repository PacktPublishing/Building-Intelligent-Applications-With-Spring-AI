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
package com.knuddels.jtokkit;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.IntArrayList;

import io.codeprimate.tools.spring.ai.tokens.jtokkit.api.EncodingWrapper;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Wrapper for JTokkit {@link GptBytePairEncoding}.
 *
 * @author John Blum
 * @see io.codeprimate.tools.spring.ai.tokens.jtokkit.api.EncodingWrapper
 * @see com.knuddels.jtokkit.GptBytePairEncoding
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class GptBytePairEncodingWrapper extends EncodingWrapper<GptBytePairEncoding> {

	public static GptBytePairEncodingWrapper wrap(Encoding encoding) {

		if (encoding instanceof GptBytePairEncoding gptEncoding) {
			return new GptBytePairEncodingWrapper(gptEncoding);
		}

		throw new IllegalArgumentException("Encoding [%s] must be a [%s]"
			.formatted(ObjectUtils.nullSafeClassName(encoding), GptBytePairEncoding.class.getName()));
	}

	private final SpecialEncoder specialEncoder;
	private final TokenEncoder tokenEncoder;

	@SuppressWarnings("all")
	public GptBytePairEncodingWrapper(GptBytePairEncoding encoding) {
		super(encoding);
		this.specialEncoder = resolveSpecialEncoder(encoding);
		this.tokenEncoder = resolveTokenEncoder(encoding);
	}

	@Nullable
	private SpecialEncoder resolveSpecialEncoder(GptBytePairEncoding encoding) {

		String fieldName = "specialEncoder";

		Field specialEncoder = ReflectionUtils.findField(GptBytePairEncoding.class, fieldName);

		Assert.state(specialEncoder != null, () -> "Failed to find field [%s] on type [%s]"
			.formatted(fieldName, encoding.getClass().getName()));

		specialEncoder.setAccessible(true);

		return (SpecialEncoder) ReflectionUtils.getField(specialEncoder, encoding);
	}

	private TokenEncoder resolveTokenEncoder(GptBytePairEncoding encoding) {
		return encoding.encoder;
	}

	@Override
	public List<String> encodeAsStringTokens(String text) {

		IntArrayList tokens = encode(text);
		List<String> stringTokens = new ArrayList<>();

		for (int index = 0, size = tokens.size(); index < size; index++) {
			int token = tokens.get(index);
			byte[] tokenBytes = getTokenEncoder().decodeToken(token, getSpecialEncoder());
			stringTokens.add(new String(tokenBytes, StandardCharsets.UTF_8));
		}

		return stringTokens;
	}
}
