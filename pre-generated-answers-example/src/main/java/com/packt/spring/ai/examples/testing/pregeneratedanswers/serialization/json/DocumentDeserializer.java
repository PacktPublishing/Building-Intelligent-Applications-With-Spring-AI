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
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.springframework.ai.document.Document;

import io.codeprimate.extensions.spring.ai.document.EmbeddedDocument;

/**
 * Jackson {@link JsonDeserializer} used to deserialize JSON as a {@link Document}.
 *
 * @author John Blum
 * @see com.fasterxml.jackson.databind.JsonDeserializer
 * @see org.springframework.ai.document.Document
 * @since 0.1.0
 */
public class DocumentDeserializer extends JsonDeserializer<Document> {

  @Override
  @SuppressWarnings("all")
  public Document deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

    JsonNode documentNode = jsonParser.getCodec().readTree(jsonParser);

    Document document = Document.builder()
      .id(parseDocumentId(documentNode))
      .text(parseDocumentContent(documentNode))
      .build();

    float[] embedding = parseDocumentEmbedding(documentNode);

    EmbeddedDocument embeddedDocument = EmbeddedDocument.from(document).withEmbedding(embedding);

    return embeddedDocument;
  }

  private String parseDocumentContent(JsonNode documentNode) {

    return documentNode.hasNonNull("content")
      ? documentNode.get("content").asText()
      : documentNode.get("text").asText();
  }

  private float[] parseDocumentEmbedding(JsonNode documentNode) {

    float[] embedding = { };

    JsonNode embeddingNode = documentNode.get("embedding");

    if (isArray(embeddingNode)) {
      ArrayNode arrayNode = (ArrayNode) embeddingNode;
      int size = arrayNode.size();
      embedding = new float[size];
      for (int index = 0; index < size; index++) {
        embedding[index] = Double.valueOf(arrayNode.get(index).asDouble()).floatValue();
      }
    }

    return embedding;
  }

  private boolean isArray(JsonNode node) {
    return node != null && node.isArray();
  }

  private String parseDocumentId(JsonNode documentNode) {

    return documentNode.hasNonNull("id")
      ? documentNode.get("id").asText()
      : UUID.randomUUID().toString();
  }
}
