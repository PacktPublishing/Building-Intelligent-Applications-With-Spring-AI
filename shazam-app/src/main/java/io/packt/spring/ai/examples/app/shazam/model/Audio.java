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
package io.packt.spring.ai.examples.app.shazam.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling audio data.
 *
 * @author John Blum
 * @see AudioSource
 * @see java.io.File
 * @see org.springframework.core.io.Resource
 * @see org.springframework.web.multipart.MultipartFile
 * @since 0.1.0
 */
@Getter
@SuppressWarnings("unused")
public class Audio implements AudioSource {

	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public static Audio decode(String data) {
		Assert.hasText(data, "Data is required");
		byte[] audioData = Base64.getDecoder().decode(data);
		return from(audioData);
	}

	public static Audio empty() {
		return from(EMPTY_BYTE_ARRAY);
	}

	public static Audio from(byte[] data) {
		return new Audio(DataSource.from(data));
	}

	public static Audio from(File audioFile) {
		return new Audio(DataSource.from(audioFile));
	}

	public static Audio from(MultipartFile audioFile) {
		return new Audio(DataSource.from(audioFile));
	}

	public static Audio from(Resource audioResource) {
		return new Audio(DataSource.from(audioResource));
	}

	public static Audio nullSafe(Audio audio) {
		return audio != null ? audio : empty();
	}

	private final DataSource dataSource;

	private Duration duration;

	private Format format;

	Audio(DataSource dataSource) {
		Assert.notNull(dataSource, "Source of audio data is required");
		this.dataSource = dataSource;
	}

	@Override
	public Audio getAudio() {
		return this;
	}

	public byte[] getData() {
		return getDataSource().getData();
	}

	public String encode() {
		return Base64.getEncoder().encodeToString(getData());
	}

	public File file() {
		return getDataSource().getFile();
	}

	public Audio havingDuration(Duration duration) {
		this.duration = duration;
		return this;
	}

	public Audio in(Format format) {
		this.format = format;
		return this;
	}

	public InputStream inputStream() {
		return getDataSource().getInputStream();
	}

	public Resource resource() {
		return getDataSource().getResource();
	}

	public long size() {
		return getDataSource().size();
	}

	public URL url() {
		return getDataSource().getUrl();
	}

	public enum Category {
		LOSSLESS_COMPRESSED, LOSSY_COMPRESSED, UNCOMPRESSED
	}

	interface DataSource {

		static DataSource from(byte[] data) {
			Assert.notNull(data, "Audio data is required");
			return () -> new ByteArrayResource(data);
		}

		static DataSource from(File file) {

			Assert.notNull(file, "Audio file is required");
			Assert.isTrue(file.isFile(), () -> "File [%s] must exist".formatted(file));

			return new DataSource() {

				@Override
				public boolean isFile() {
					return true;
				}

				@Override
				public File getFile() {
					return file;
				}

				@Override
				public InputStream getInputStream() {
					return ExceptionThrowingSupplier.getSafely(() -> new FileInputStream(file));
				}

				@Override
				public Resource getResource() {
					return new FileSystemResource(file);
				}

				@Override
				public URL getUrl() {
					return ExceptionThrowingSupplier.getSafely(file.toURI()::toURL);
				}

				@Override
				public long size() {
					return file.length();
				}
			};
		}

		static DataSource from(MultipartFile file) {

			Assert.notNull(file, "Audio file is required");

			return new DataSource() {

				@Override
				public boolean isFile() {
					return true;
				}

				@Override
				public byte[] getData() {
					return ExceptionThrowingSupplier.getSafely(file::getBytes);
				}

				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}

				@Override
				public Resource getResource() {
					return file.getResource();
				}

				@Override
				public long size() {
					return file.getSize();
				}
			};
		}

		static DataSource from(Resource resource) {
			Assert.notNull(resource, "Audio resource is required");
			return () -> resource;
		}

		default boolean isFile() {
			return getResource().isFile();
		}

		default byte[] getData() {
			return ExceptionThrowingSupplier.getSafely(getResource()::getContentAsByteArray);
		}

		default File getFile() {
			Assert.state(isFile(), "DataSource did not originate from a file");
			return ExceptionThrowingSupplier.getSafely(getResource()::getFile);
		}

		default String getFilename() {
			return getFile().getName();
		}

		default String getFilepath() {
			return getFile().getAbsolutePath();
		}

		default InputStream getInputStream() {
			return new ByteArrayInputStream(getData());
		}

		Resource getResource();

		default URL getUrl() {
			return ExceptionThrowingSupplier.getSafely(getResource()::getURL);
		}

		default long size() {
			return getData().length;
		}
	}

	/**
	 * @see <a href="https://en.wikipedia.org/wiki/Audio_file_format">Audio file format</a>
	 */
	@Getter
	@ToString(of = "name")
	public enum Format {

		AAC("Advanced Audio Coding", Category.LOSSY_COMPRESSED),
		AIFF("Audio Interchange File Format", Category.UNCOMPRESSED),
		ALAC("Apple Lossless Audio Codec", Category.LOSSLESS_COMPRESSED),
		AU("Sun Microsystems UNIX Audio File Format", Category.UNCOMPRESSED),
		DSD("Direct Stream Digital", Category.UNCOMPRESSED),
		FLAC("Free Lossless Audio Codec", Category.LOSSLESS_COMPRESSED),
		MP3("MPEG-1 Audio Layer III", Category.LOSSLESS_COMPRESSED),
		MP4("MPEG-4 Part 14", Category.LOSSY_COMPRESSED),
		WAV("Waveform Audio File Format", Category.UNCOMPRESSED),
		WMA("Microsoft Media Audio", Category.UNCOMPRESSED);

		private final Category category;

		private final String name;

		Format(String name, Category category) {
			this.name = name;
			this.category = category;
		}

		public boolean isLossy() {
			return Category.LOSSY_COMPRESSED.equals(getCategory());
		}

		public boolean isLossless() {
			return Set.of(Category.UNCOMPRESSED, Category.LOSSLESS_COMPRESSED).contains(getCategory());
		}
	}
}
