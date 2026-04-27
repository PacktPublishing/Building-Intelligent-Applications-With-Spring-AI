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
package io.codeprimate.extensions.spring.core.http;

import java.io.IOException;
import java.io.InputStream;

import io.codeprimate.extensions.java.io.CachingInputStream;

import org.cp.elements.lang.Assert;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

/**
 * {@link ClientHttpResponse} implementation used to wrap an existing {@link ClientHttpResponse}
 * to decorate the operations.
 *
 * @author John Blum
 * @see ClientHttpResponse
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface ClientHttpResponseWrapper extends ClientHttpResponse {

	static ClientHttpResponse wrap(ClientHttpResponse response) {

		Assert.notNull(response, "ClientHttpResponse to wrap is required");

		return new ClientHttpResponse() {

			@Override
			public @NonNull InputStream getBody() throws IOException {
				return CachingInputStream.from(response.getBody());
			}

			@Override
			public void close() {
				response.close();
			}

			@Override
			public @NonNull HttpHeaders getHeaders() {
				return response.getHeaders();
			}

			@Override
			public @NonNull HttpStatusCode getStatusCode() throws IOException {
				return response.getStatusCode();
			}

			@Override
			public @NonNull String getStatusText() throws IOException {
				return response.getStatusText();
			}
		};
	}
}
