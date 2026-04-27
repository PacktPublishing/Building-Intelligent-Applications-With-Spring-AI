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
package io.codeprimate.extensions.java.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.codeprimate.extensions.util.ExceptionThrowingSupplier;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * {@link InputStream} implementation that caches the data from the wrapped {@link InputStream}.
 *
 * @author John Blum
 * @see java.io.InputStream
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class CachingInputStream extends InputStream {

	public static CachingInputStream from(InputStream in) {
		return ExceptionThrowingSupplier.getSafely(() -> new CachingInputStream(in), cause -> {
			throw new RuntimeException("Failed to cache InputStream", cause);
		});
	}

	private volatile boolean close = false;

	private int index;

	private final byte[] data;

	public CachingInputStream(InputStream in) throws IOException {
		Assert.notNull(in, "InputStream to cache is required");
		this.data = in.readAllBytes();
	}

	protected void assertNotClosed() {
		Assert.state(!this.close, "InputStream is closed");
	}

	@Override
	public int available() {
		return this.data.length - index;
	}

	@Override
	public void close() {
		this.close = true;
	}

	@Override
	public int read() throws IOException {
		assertNotClosed();
		int readIndex = this.index++;
		return Byte.valueOf(this.data[readIndex]).intValue();
	}

	@SuppressWarnings("unused")
	private byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
		in.transferTo(out);
		return out.toByteArray();
	}

	@Override
	public @NonNull byte[] readAllBytes() {
		assertNotClosed();
		byte[] dataCopy = new byte[this.data.length];
		System.arraycopy(this.data, 0, dataCopy, 0, dataCopy.length);
		return dataCopy;
	}

	@Override
	public synchronized void reset() {
		assertNotClosed();
		this.index = 0;
	}
}
