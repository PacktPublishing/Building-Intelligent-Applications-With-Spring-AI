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
package io.codeprimate.extensions;

/**
 * Java program outputting the version of the Codeprimate Extensions library.
 *
 * @author John Blum
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class CodeprimateExtensionsApplication {

	protected static final String SPRING_PROFILE = "codeprimate-extensions-version-app";
	protected static final String VERSION = "0.1.0";

	public static void main(String[] args) {
		System.out.printf("Codeprimate Extensions version %s%n".formatted(VERSION));
	}
}
