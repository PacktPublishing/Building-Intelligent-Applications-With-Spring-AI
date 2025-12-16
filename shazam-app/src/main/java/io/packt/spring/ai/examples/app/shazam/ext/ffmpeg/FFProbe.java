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
package io.packt.spring.ai.examples.app.shazam.ext.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * API for {@literal ffprobe}.
 *
 * @author John Blum
 * @see JsonMapper
 * @see <a href="https://ffmpeg.org/ffprobe.html">ffprobe Documentation</a>
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
public class FFProbe {

	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("ffprobe.debug", "false"));

	protected static final String PROGRAM_NAME = "ffprobe";

	protected static final File PROGRAM = new File(new File(System.getProperty("user.home"), "bin"), PROGRAM_NAME);

	protected static final List<String> FFPROBE_VERSION_COMMAND =
		List.of(PROGRAM.getAbsolutePath(), "-v", "quiet", "-version");

	protected static final List<String> FFPROBE_SHOW_FORMAT_COMMAND =
		List.of(PROGRAM.getAbsolutePath(), "-v", "quiet", "-show_format", "-of", "json");

	private final JsonMapper jsonMapper;

	public FFProbe() {

		Assert.state(PROGRAM.isFile(), () -> "ffprobe [%s] not found".formatted(PROGRAM_NAME));

		int exitCode = ExceptionThrowingSupplier.getSafely(() -> {
			Process process = run(FFPROBE_VERSION_COMMAND);
			String output = readOutput(process);
			log(output);
			return process.waitFor();
		});

		Assert.state(exitCode == 0, () -> "ffprobe is not available; exit code was [%d]".formatted(exitCode));

		this.jsonMapper = JsonMapper.builder()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
			.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
			.configure(SerializationFeature.INDENT_OUTPUT, true)
			.build();
	}

	private void log(String message, Object... arguments) {
		if (DEBUG) {
			System.out.printf(message, arguments);
			System.out.flush();
		}
	}

	private Process run(List<String> command) throws IOException {
		return new ProcessBuilder(command).start();
	}

	private String readOutput(Process process) throws IOException {

		StringBuilder output = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				output.append(line);
			}
		}

		return output.toString().trim();
	}

	public Format showFormat(Audio audio) {

		try {
			log("URL [%s]%n", audio.resource().getURL());

			File audioFile = audio.file();
			String audioFilePath = audioFile.getAbsolutePath();
			List<String> command = new ArrayList<>(FFPROBE_SHOW_FORMAT_COMMAND);

			command.add(audioFilePath);
			log("Command [%s]%n", String.join(" ", command));

			Process process = run(command);
			String json = readOutput(process);

			log("JSON [%s]%n", json);

			Probe probe = getJsonMapper().readValue(json, Probe.class);

			return probe.format();
		}
		catch (IOException cause) {
			String message = "Failed to probe audio [%s]".formatted(audio);
			throw new ProbeException(message, cause);
		}
	}

	@SuppressWarnings("unused")
	static class ProbeException extends RuntimeException {

		ProbeException() {

		}

		ProbeException(String message) {
			super(message);
		}

		ProbeException(Throwable cause) {
			super(cause);
		}

		ProbeException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	record Probe(@JsonProperty("format") Format format) {

	}

	public record Format(
		@JsonProperty("start_time") Double startTime,
		@JsonProperty("duration") Double duration, // Duration in (fractional) seconds
		@JsonProperty("bit_rate") Integer bitRate,
		@JsonProperty("nb_programs") Integer numberOfPrograms,
		@JsonProperty("nb_streams") Integer numberOfStreams,
		@JsonProperty("nb_stream_groups") Integer numberOfStreamGroups,
		@JsonProperty("probe_score") Integer probeScore,
		@JsonProperty("size") Integer size, // Size in bytes
		@JsonProperty("format_long_name") String description,
		@JsonProperty("filename") String filename,
		@JsonProperty("format_name") String name) {

		public Duration getDuration() {
			long seconds = Math.round(duration());
			return Duration.ofSeconds(seconds);
		}
	}
}
