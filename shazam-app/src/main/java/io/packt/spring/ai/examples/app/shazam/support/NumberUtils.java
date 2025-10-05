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
package io.packt.spring.ai.examples.app.shazam.support;

/**
 * Abstract utility class used to process {@link Number numbers}.
 *
 * @author John Blum
 * @since 0.1.0
 */
public abstract class NumberUtils {

	public static float asFloat(int value) {
		return Integer.valueOf(value).floatValue();
	}

	public static int asInt(float value) {
		return Float.valueOf(value).intValue();
	}

	public static int asInt(long value) {
		return Long.valueOf(value).intValue();
	}
}
