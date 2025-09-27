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
package io.packt.spring.ai.examples.app.shazam.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring Web MVC {@link Controller} used to show the main Shazam application Web page.
 *
 * @author John Blum
 * @see Controller
 * @see RequestMapping
 * @since 0.1.0
 */
@Controller
@RequestMapping("/view")
@SuppressWarnings("unused")
public class ShazamViewController {

	@GetMapping("/recorder")
	public String whatsMyJamView() {
		return "WhatsMyJam.html";
	}
}
