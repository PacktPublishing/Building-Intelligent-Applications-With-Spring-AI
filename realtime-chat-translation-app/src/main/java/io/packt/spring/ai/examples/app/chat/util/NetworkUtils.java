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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.cp.elements.function.ThrowableSupplier;
import org.springframework.core.env.Environment;

/**
 * Abstract utility class for processing {@link URL} objects.
 *
 * @author John Blum
 * @since 0.1.0
 */
public abstract class NetworkUtils {

	public static final int DEFAULT_SERVER_PORT = 8080;

	public static final String NETWORK_IP_ADDRESS_PREFIX_PROPERTY = "app.network.host.ip-address-prefix";
	public static final String WEB_PATH_SEPARATOR = "/";

	@SuppressWarnings("unused")
	public static String resolveHostIpAddress(String hostname) {
		return getSafely(() -> InetAddress.getByName(hostname).getHostAddress(), cause -> {
			String message = "Failed to resolve host's [%s] IP address".formatted(hostname);
			throw new IllegalStateException(message, cause);
		});
	}

	public static String resolveLocalhostIpAddress(Environment environment) {

		String networkIpAddressPrefix = environment.getProperty(NETWORK_IP_ADDRESS_PREFIX_PROPERTY);

		List<NetworkInterface> networkInterfaces = streamNetworkInterfaces()
			.filter(networkInterfacePredicate())
			.toList();

		for (NetworkInterface networkInterface : networkInterfaces) {
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				String hostAddress = nullSafeHostAddress(address);
				if (hostAddress.startsWith(networkIpAddressPrefix)) {
					return hostAddress;
				}
			}
		}

		throw new IllegalStateException("Failed to resolve localhost IP address");
	}

	private static Predicate<NetworkInterface> networkInterfacePredicate() {

		return networkInterface -> {
			try {
				return networkInterface.isUp()
					&& isNot(networkInterface.isLoopback())
					&& isNot(networkInterface.isVirtual());
			}
			catch (SocketException e) {
				return false;
			}
		};
	}

	private static String nullSafeHostAddress(InetAddress address) {
		return String.valueOf(address.getHostAddress());
	}

	private static Stream<NetworkInterface> streamNetworkInterfaces() {
		return getSafely(NetworkInterface::networkInterfaces, cause -> {
			throw new IllegalStateException("Failed to resolve NetworkInterfaces", cause);
		});
	}

	public static String resolveLocalhostName() {
		return getSafely(() -> InetAddress.getLocalHost().getHostName(), cause -> {
			throw new IllegalStateException("Failed to resolve localhost name", cause);
		});
	}

	public static URI resolveUri(String baseUrl, Object... pathElements) {
		return URI.create(baseUrl.formatted(pathElements));
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

	private static boolean isNot(boolean condition) {
		return !condition;
	}

	private static <T> T getSafely(ThrowableSupplier<T> supplier, Function<Exception, T> exceptionHandler) {
		try {
			return supplier.get();
		}
		catch (Exception cause) {
			return exceptionHandler.apply(cause);
		}
	}
}
