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

import com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Questions;
import com.packt.spring.ai.examples.testing.pregeneratedanswers.util.Utils;

import org.springframework.boot.jackson.JacksonComponent;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Spring {@link JacksonComponent} and Jackson {@link ValueSerializer} used to serialize {@link Questions} to JSON.
 *
 * @author John Blum
 * @see JacksonComponent
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Question
 * @see com.packt.spring.ai.examples.testing.pregeneratedanswers.model.Questions
 * @see tools.jackson.core.JsonGenerator
 * @see tools.jackson.databind.ValueSerializer
 * @since 0.1.0
 */
@JacksonComponent
@SuppressWarnings("unused")
public class QuestionsSerializer extends ValueSerializer<Questions> {


	@Override
	public void serialize(Questions questions, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
		jsonGenerator.writeStartArray();
		questions.stream().forEach(Utils.consumeSafely(jsonGenerator::writePOJO));
		jsonGenerator.writeEndArray();
	}
}
