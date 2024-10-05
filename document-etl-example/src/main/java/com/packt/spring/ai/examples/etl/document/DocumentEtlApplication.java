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
package com.packt.spring.ai.examples.etl.document;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * {@link SpringBootApplication} using Spring AI with Ollama to demonstrate ETL capabilities with Spring AI
 * using the {@link Document} API.
 *
 * @author John Blum
 * @see org.springframework.ai.chat.model.ChatModel
 * @see org.springframework.ai.document.Document
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 0.1.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class DocumentEtlApplication {

	private static final String DOCUMENT_NAME = "WhatIsArtificialIntelligenceByIBM.pdf";
	private static final String EXCERPT_KEYWORDS = "excerpt_keywords";

	public static void main(String[] args) {

		new SpringApplicationBuilder(DocumentEtlApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

		private final AtomicReference<List<Document>> documentsReference = new AtomicReference<>(null);

		private final Resource pdfDocument = new ClassPathResource(DOCUMENT_NAME);

	@Bean
	@Order(1)
	ApplicationRunner extractRunner() {

		return args -> {

			System.out.printf("Extracting/Ingesting Pages from Document [%s]%n", DOCUMENT_NAME);

			PdfDocumentReaderConfig documentReaderConfiguration =
				PdfDocumentReaderConfig.builder()
					.withPagesPerDocument(1)
					.build();

			PagePdfDocumentReader pdfReader =
				new PagePdfDocumentReader(this.pdfDocument, documentReaderConfiguration);

			List<Document> pdfDocumentPages = pdfReader.read();

			this.documentsReference.set(pdfDocumentPages);
		};
	}

	@Bean
	@Order(2)
	ApplicationRunner keywordTransformRunner(ChatModel chatModel) {

		return args -> this.documentsReference.updateAndGet(documents -> {

			System.out.printf("Transforming Documents (%d)%n", documents.size());

			KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(chatModel, 25);

			return keywordMetadataEnricher.transform(documents);
		});
	}

	@Bean
	@Order(3)
	ApplicationRunner loadRunner() {
		return args -> StandardOutKeywordDocumentWriter.INSTANCE.accept(this.documentsReference.get());
	}

	static class StandardOutKeywordDocumentWriter implements DocumentWriter {

		static final StandardOutKeywordDocumentWriter INSTANCE = new StandardOutKeywordDocumentWriter();

		@Override
		public void accept(List<Document> documentPages) {

			System.out.printf("Keywords in Document [%s] by page:%n", DOCUMENT_NAME);

			IntStream.range(0, documentPages.size()).forEach(pageIndex -> {
				int pageNumber = pageIndex + 1;
				Document page = documentPages.get(pageIndex);
				System.out.printf("%d - %s%n".formatted(pageNumber, page.getMetadata().get(EXCERPT_KEYWORDS)));
			});
		}
	}
}
