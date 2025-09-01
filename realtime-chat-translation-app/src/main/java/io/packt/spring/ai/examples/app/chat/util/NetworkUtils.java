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
package io.packt.spring.ai.examples.app.chat.util;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Abstract utility class for processing {@link URL} objects.
 *
 * @author John Blum
 * @since 0.1.0
 */
public abstract class NetworkUtils {

	public static final int DEFAULT_SERVER_PORT = 8080;

	public static final String LOCALHOST = "localhost";
	public static final String WEB_PATH_SEPARATOR = "/";

	public static String resolveIpAddress(String hostname, int port) {
		return InetSocketAddress.createUnresolved(hostname, port).getHostName();
	}

	public static String resolveIpAddress(String hostname) {
		return resolveIpAddress(hostname, DEFAULT_SERVER_PORT);
	}

	public static String resolveLocalhostIpAddress() {
		return resolveIpAddress(LOCALHOST);
	}

	public static URI resolveUri(String baseUrl, Object... elements) {
		return URI.create(baseUrl.formatted(elements));
	}

	public static URL toUrl(URI uri) {

		try  {
			return uri.toURL();
		}
		catch (MalformedURLException cause) {
			String message = "Failed to construct URL from URI [%s]".formatted(uri);
			throw new IllegalArgumentException(message, cause);
		}
	}
}
