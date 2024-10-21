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
package io.codeprimate.tools.spring.ai.tokens.web.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.codeprimate.extensions.util.Utils;
import io.codeprimate.tools.spring.ai.tokens.model.Document;
import io.codeprimate.tools.spring.ai.tokens.service.TokenizerService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Web MVC {@link Controller} used to present the token metadata application UI.
 *
 * @author John Blum
 * @see org.springframework.stereotype.Controller
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @since 0.1.0
 */
@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class TokenMetadataViewController {

	protected static final String INDEX = "index";

	protected static final String TOKEN_HTML_TEMPLATE = "<span style=\"background: %s; color:white;\">%s</span>";

	protected static final String[] HEX_COLORS = { "#b41d16", "#2a81d8", "#f3b414", "#823eaf", "#208720" };

	private final TokenizerService tokenizerService;

	@GetMapping
	public String index() {
		return INDEX;
	}

	@PostMapping("/tokens")
	@SuppressWarnings("all")
	public @ResponseBody String tokens(@RequestBody Document document) {

		List<String> tokens = getTokenizerService().tokenize(document.content());

		AtomicInteger counter = new AtomicInteger(0);

		String html = tokens.stream()
			.map(token -> TOKEN_HTML_TEMPLATE.formatted(HEX_COLORS[resolveIndex(counter.getAndIncrement())], token))
			.reduce("%s%s"::formatted)
			.orElse(Utils.EMPTY_STRING);

		return html;
	}

	private int resolveIndex(int count) {
		return count > 0 ? count % HEX_COLORS.length : 0;
	}
}
