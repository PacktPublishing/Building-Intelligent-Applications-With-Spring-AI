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
package io.codeprimate.extensions.spring.boot.web.contoller;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.cp.elements.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Web MVC {@link RestController} providing administrative functions for a Web application.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @since 0.1.0
 */
@RestController
@RequestMapping("/admin")
@SuppressWarnings("unused")
public class AdminController {

	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DEFAULT_NAME = "World";
	private static final String HELLO = "Hello %s";
	private static final String PONG = "PONG";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

	@GetMapping("/hello/{name}")
	public String hello(@PathVariable(required = false) String name) {
		return HELLO.formatted(resolveName(name));
	}

	String resolveName(String name) {
		String resolvedName = StringUtils.defaultIfBlank(name, username());
		resolvedName = StringUtils.defaultIfBlank(resolvedName, DEFAULT_NAME);
		return resolvedName;
	}

	String username() {
		return System.getProperty("user.name");
	}

	@GetMapping("/ping")
	public String ping() {
		return PONG;
	}

	@GetMapping("/time")
	public String time() {
		return ZonedDateTime.now().format(DATE_TIME_FORMATTER);
	}
}
