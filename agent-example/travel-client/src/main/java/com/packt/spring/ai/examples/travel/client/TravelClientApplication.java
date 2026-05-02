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
package com.packt.spring.ai.examples.travel.client;

import static org.cp.elements.lang.LangExtensions.assertThat;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.codeprimate.extensions.spring.boot.AbstractSpringBootApplication;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.cp.elements.lang.Assert;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * {@link SpringBootApplication} and {@literal MCP client} for the Travel Agent application.
 *
 * @author John Blum
 * @see SpringBootApplication
 * @see ApplicationRunner
 * @since 0.1.0
 */
@SpringBootApplication
@Profile(TravelClientApplication.TRAVEL_CLIENT_PROFILE)
public class TravelClientApplication extends AbstractSpringBootApplication {

	protected static final String DATE_PATTERN = "yyyy-MM-dd";
	protected static final String JAVA_LAUNCHER = "java";
	protected static final String SEARCH_FLIGHTS_TOOL_NAME = "flight-search";
	protected static final String TRAVEL_AGENT_JAR = "travel-agent-0.1.0-SNAPSHOT.jar";
	protected static final String TRAVEL_CLIENT_PROFILE = "travel-client";

	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

	public static void main(String[] args) {
		runSpringApplication(TravelClientApplication.class, asStringArray(TRAVEL_CLIENT_PROFILE), args);
	}

	private static String formatDate(ZonedDateTime dateTime) {
		return dateTime.format(DATE_FORMATTER);
	}

	@Bean
	ApplicationRunner programRunner(ObjectMapper objectMapper) {

		return applicationArguments -> {

			File pathToTravelAgentJar = resolveTravelAgentJar(applicationArguments);

			var serverParameters = new ServerParameters.Builder(JAVA_LAUNCHER)
				.args("-jar", pathToTravelAgentJar.getAbsolutePath())
				.build();

			var transport = new StdioClientTransport(serverParameters, objectMapper);

			try (var client = McpClient.sync(transport).build()) {

				client.initialize();

				McpSchema.ListToolsResult listToolsResult = client.listTools();

				List<McpSchema.Tool> tools = listToolsResult.tools();

				print("TOOLS");

				tools.forEach(tool -> {
					print("Tool Name [%s] and Description [%s] with Input Schema [%s]%n",
						tool.name(), tool.description(), tool.inputSchema());
				});

				searchFlights(client);
			}
		};
	}

	private static void searchFlights(McpSyncClient client) {

		ZonedDateTime now = ZonedDateTime.now();

		McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(SEARCH_FLIGHTS_TOOL_NAME, Map.of(
			"departureAirport", "PDX",
			"departureDate", formatDate(now.plusWeeks(3)),
			"arrivalAirport", "SFO",
			"returnDate", formatDate(now.plusWeeks(3).plusDays(5)),
			"airlineName", "United",
			"stops", "1"
		));

		McpSchema.CallToolResult result = client.callTool(request);

		assertThat(result).isNotNull();

		print("CONTENT [%s]", result.content());
	}

	private File resolveTravelAgentJar(ApplicationArguments applicationArguments) {

		File basepath = resolveBasepath(applicationArguments);
		File travelAgentJar = new File(basepath, TRAVEL_AGENT_JAR);

		Assert.isTrue(travelAgentJar.isFile(), "[%s] is not the expected Travel Agent JAR [%s]",
			travelAgentJar, TRAVEL_AGENT_JAR);

		return travelAgentJar;
	}

	private File resolveBasepath(ApplicationArguments applicationArguments) {

		String[] arguments = applicationArguments.getSourceArgs();

		File basepath = arguments.length > 0 ? new File(arguments[0])
			: new File(System.getProperty("user.home"));

		Assert.isTrue(basepath.isDirectory(), "Basepath [%s] must be a directory".formatted(basepath));

		return basepath;
	}
}
