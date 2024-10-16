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
package com.packt.spring.ai.examples.testing.pregeneratedanswers.serialization.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Questions;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.boot.jackson.JsonComponent;

/**
 * Spring {@link JsonComponent} and Jackson {@link JsonSerializer} used to serialize {@link Questions} to JSON.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Questions
 * @see com.fasterxml.jackson.core.JsonGenerator
 * @see com.fasterxml.jackson.databind.JsonSerializer
 * @see org.springframework.boot.jackson.JsonComponent
 * @since 0.1.0
 */
@JsonComponent
@SuppressWarnings("unused")
public class QuestionsSerializer extends JsonSerializer<Questions> {

	@Override
	public void serialize(Questions questions, JsonGenerator jsonGenerator, SerializerProvider serializers)
			throws IOException {

		jsonGenerator.writeStartArray();
		questions.stream().forEach(Utils.consumeSafely(jsonGenerator::writeObject));
		jsonGenerator.writeEndArray();
	}
}
