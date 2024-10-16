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
package com.packt.spring.ai.examples.testing.pregeneratedanswers;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to test with pre-generated answers.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class PreGeneratedAnswersApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(PreGeneratedAnswersApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}
}
