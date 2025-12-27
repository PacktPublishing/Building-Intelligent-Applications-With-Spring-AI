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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.cp.elements.io.FileSystemUtils;
import org.cp.elements.lang.Assert;
import org.cp.elements.util.ArrayUtils;
import org.cp.elements.util.zip.ZipUtils;
import org.slf4j.Logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * API for {@literal ffprobe}.
 *
 * @author John Blum
 * @see Audio
 * @see <a href="https://ffmpeg.org/ffprobe.html">ffprobe Documentation</a>
 * @since 0.1.0
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public class FFProbe {

	public static final String PROGRAM_NAME = "ffprobe";
	public static final String PROGRAM_VERSION = "8.0.1";

	private static final int BUFFER_SIZE = 32_768;

	private static final Map<Object, Object> cache = new ConcurrentHashMap<>();

	private static final String PROGRAM_FILENAME = PROGRAM_NAME;
	private static final String ZIP_FILENAME = "%s-%s.zip".formatted(PROGRAM_NAME, PROGRAM_VERSION);

	private static final URI FFPROBE_URI = URI.create("https://evermeet.cx/ffmpeg/%s".formatted(ZIP_FILENAME));

	public static Builder builder() {
		return new Builder();
	}

	private final File program;

	private final JsonMapper jsonMapper;

	protected FFProbe(File program) {
		this.program = assertProgram(program);
		this.jsonMapper = buildJsonMapper();
	}

	private File assertProgram(File program) {
		Assert.notNull(program, "ffprobe is required");
		Assert.state(program.isFile(), () -> "ffprobe [%s] not found".formatted(program));
		return program;
	}

	private JsonMapper buildJsonMapper() {
		return JsonMapper.builder()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
			.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
			.configure(SerializationFeature.INDENT_OUTPUT, true)
			.build();
	}

	@SuppressWarnings("unchecked")
	protected <VALUE> Map<Object, VALUE> getCache() {
		return (Map<Object, VALUE>) cache;
	}

	protected Logger getLogger() {
		return log;
	}

	protected void logDebug(Supplier<String> message) {

		Logger logger = getLogger();

		if (logger.isDebugEnabled()) {
			logger.debug(message.get());
		}
	}

	protected Process run(CommandLine commandLine) throws IOException {
		return new ProcessBuilder(commandLine.getCommand()).start();
	}

	protected <T> T run(CommandLine commandLine, Class<T> type) throws IOException {

		Process process = run(commandLine);
		String output = readOutput(process);

		return String.class.equals(type) ? type.cast(output)
			: readJson(output, type);
	}

	public Format showFormat(Audio audio) {

		Probe cachedProbe = this.<Probe>getCache().computeIfAbsent(audio, key -> {
			try {
				CommandLine showFormat = CommandLine.run(getProgram())
					.withArguments("-v", "quiet", "-show_format", "-of", "json")
					.withArgument(audio)
					.log();

				Probe probe = run(showFormat, Probe.class);

				logDebug(() -> "Probe [%s]".formatted(probe));

				return probe;
			}
			catch (IOException cause) {
				throw ProbeException.because("Failed to probe format of audio", cause);
			}
		});

		return cachedProbe.format();
	}

	@SuppressWarnings("all")
	public int version() {

		int exitCode = ExceptionThrowingSupplier.getSafely(() -> {
			CommandLine version = CommandLine.run(getProgram()).withArguments("-v", "quiet", "-version").log();
			Process process = run(version);
			String output = readOutput(process);
			getLogger().info(output);
			return process.waitFor();
		});

		return exitCode;
	}

	private <T> T readJson(String json, Class<T> type) throws IOException {
		return getJsonMapper().readValue(json, type);
	}

	private String readOutput(Process process) throws IOException {

		StringBuilder output = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				output.append(line);
				output.append(System.lineSeparator());
			}
		}

		String processOutput = output.toString().trim();

		logDebug(() -> "Process Output [%s]".formatted(processOutput));

		return processOutput;
	}

	public record Probe(@JsonProperty("format") Format format) {

	}

	public record Format(
		@JsonProperty("bit_rate") Integer bitRate,
		@JsonProperty("duration") Double duration, // Duration in (fractional) seconds
		@JsonProperty("filename") String filename,
		@JsonProperty("format_name") String name,
		@JsonProperty("format_long_name") String description,
		@JsonProperty("nb_programs") Integer numberOfPrograms,
		@JsonProperty("nb_streams") Integer numberOfStreams,
		@JsonProperty("nb_stream_groups") Integer numberOfStreamGroups,
		@JsonProperty("probe_score") Integer probeScore,
		@JsonProperty("size") Integer size, // Size in bytes
		@JsonProperty("start_time") Double startTime
	) {

		public Duration getDuration() {
			long seconds = Math.round(duration());
			return Duration.ofSeconds(seconds);
		}
	}

	@SuppressWarnings("unused")
	@Getter(AccessLevel.PROTECTED)
	public static class Builder implements org.cp.elements.lang.Builder<FFProbe> {

		private boolean downloadEnabled;

		private File directory;
		private File program;

		public Builder downloadTo(File directory) {
			Assert.notNull(directory, "Directory is required");
			Assert.isTrue(directory.isDirectory() || directory.mkdirs(), "Location [%s] must be a directory", directory);
			this.directory = directory;
			this.downloadEnabled = true;
			return this;
		}

		public Builder enableDownload() {
			this.downloadEnabled = true;
			return this;
		}

		public Builder using(File program) {
			this.program = program;
			return this;
		}

		private boolean isProgramPresent() {
			File program = getProgram();
			return program != null && program.isFile();
		}

		private File resolveProgram() {

			if (isProgramPresent()) {
				File program = getProgram();
				Assert.state(program.getName().equals(PROGRAM_NAME),
					"Program must be [%s]; but was [%s]", PROGRAM_NAME, program);
				return program;
			}
			else {
				return download();
			}
		}

		private File download() {

			File downloadDirectory = downloadDirectory();
			File program = new File(downloadDirectory, PROGRAM_FILENAME);

			if (program.isFile()) {
				return program;
			}

			try {
				File zipFile = new File(downloadDirectory, ZIP_FILENAME);

				if (zipFile.isFile()) {
					return unzip(zipFile);
				}

				Assert.state(isDownloadEnabled(), "Path to [%s] is invalid and download was not enabled", PROGRAM_NAME);

				byte[] buffer = new byte[BUFFER_SIZE];

				try (FileOutputStream out = new FileOutputStream(zipFile)) {
					try (DataInputStream in = new DataInputStream(openConnection(FFPROBE_URI))) {
						for (int bytesRead = in.read(buffer); bytesRead != -1; bytesRead = in.read(buffer)) {
							out.write(buffer, 0, bytesRead);
							out.flush();
							System.out.print(".");
							System.out.flush();
						}
					}
				}

				return setExecuteMode(unzip(zipFile));
			}
			catch (IOException cause) {
				String message = "Failed to download %s from [%s]".formatted(PROGRAM_NAME, FFPROBE_URI);
				throw new RuntimeException(message, cause);
			}
		}

		@SuppressWarnings("all")
		private File downloadDirectory() {

			File configuredDirectory = getDirectory();

			File downloadDirectory = configuredDirectory != null ? configuredDirectory
				: FileSystemUtils.WORKING_DIRECTORY;

			return downloadDirectory;
		}

		private InputStream openConnection(URI uri) throws IOException {
			return uri.toURL().openConnection().getInputStream();
		}

		private File setExecuteMode(File program) throws IOException {
			Process process = Runtime.getRuntime().exec(ArrayUtils.asArray("chmod", "ug+x", program.getAbsolutePath()));
			int exitCode = ExceptionThrowingSupplier.getSafely(process::waitFor, cause -> {
				String message = "Failed to set execute mode of program [%s]".formatted(program);
				throw new RuntimeException(message, cause);
			});
			return program;
		}

		@SuppressWarnings("all")
		private File unzip(File zipFile) throws IOException {
			File location = zipFile.getParentFile();
			ZipUtils.unzip(zipFile, location);
			File program = new File(location, PROGRAM_FILENAME);
			Assert.isTrue(program.isFile(), "Failed to unzip file [%s]", zipFile);
			return program;
		}

		public FFProbe build() {
			FFProbe ffprobe = new FFProbe(resolveProgram());
			int exitCode = ffprobe.version();
			Assert.state(exitCode == 0, "%s is not available; exit code was [%d]", PROGRAM_NAME, exitCode);
			return ffprobe;
		}
	}

	@SuppressWarnings("unused")
	protected static class CommandLine {

		static CommandLine run(File program) {
			return new CommandLine(program);
		}

		private final File program;

		private final List<String> arguments;

		private CommandLine(File program) {
			Assert.notNull(program, "Program to run is required");
			this.program = program;
			this.arguments = new ArrayList<>();
		}

		protected CommandLine log() {
			log.debug("Command-Line [{}]", this);
			return this;
		}

		protected CommandLine withArgument(Audio audio) {
			File audioFile = audio.file();
			String audioFilePath = audioFile.getAbsolutePath();
			this.arguments.add(audioFilePath);
			return this;
		}

		protected CommandLine withArguments(String... arguments) {
			Collections.addAll(this.arguments, arguments);
			return this;
		}

		protected List<String> getCommand() {
			List<String> command = new ArrayList<>(List.of(this.program.getAbsolutePath()));
			command.addAll(this.arguments);
			return command;
		}

		@Override
		public String toString() {
			return getCommand().toString();
		}
	}

	@SuppressWarnings("unused")
	protected static class ProbeException extends RuntimeException {

		static ProbeException because(String message, Throwable cause) {
			return new ProbeException(message, cause);
		}

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
}
