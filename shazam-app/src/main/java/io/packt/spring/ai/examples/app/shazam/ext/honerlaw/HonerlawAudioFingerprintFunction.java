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
package io.packt.spring.ai.examples.app.shazam.ext.honerlaw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.honerlaw.audio.fingerprint.AudioFile;
import io.honerlaw.audio.fingerprint.hash.FingerPrint;
import io.honerlaw.audio.fingerprint.hash.peak.HashedPeak;
import io.honerlaw.audio.fingerprint.util.InputStreamSource;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintException;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.Fingerprint;
import io.packt.spring.ai.examples.app.shazam.model.Audio;
import io.packt.spring.ai.examples.app.shazam.support.NumberUtils;

import org.cp.elements.lang.Assert;

/**
 * {@link AudioFingerprintFunction} implementation based on {@literal Honerlaw's Audio Fingerprinting Library}.
 *
 * @author John Blum
 * @see Audio
 * @see AudioFingerprintFunction
 * @see Fingerprint
 * @see io.honerlaw.audio.fingerprint.hash.FingerPrint
 * @see <a href="https://github.com/honerlaw/audio-fingerprinting">Honerlaw Audio Fingerprinting</a>
 * @since 0.1.0
 */
public class HonerlawAudioFingerprintFunction implements AudioFingerprintFunction<List<HashedPeak>> {

	@Override
	public Fingerprint<List<HashedPeak>> compute(Audio audio) {

		Assert.notNull(audio, "Audio is required");

		try {
			FingerPrint fingerPrint = new FingerPrint(newAudioFile(audio));

			HashedPeak[] hashes = fingerPrint.getHashes();

			return HonerlawFingerprint.from(hashes);
		}
		catch (Exception cause) {
			throw AudioFingerprintException.because("Failed to compute fingerprint for Audio", cause);
		}
	}

	private AudioFile newAudioFile(Audio audio) throws Exception {
		return new AudioFile(toInputStreamSource(audio));
	}

	protected InputStreamSource toInputStreamSource(Audio audio) {
		return audio::inputStream;
	}

	protected record HonerlawFingerprint(List<HashedPeak> hashes) implements Fingerprint<List<HashedPeak>> {

		protected HonerlawFingerprint {
			Assert.notNull(hashes, "Audio hashes are required");
		}

		protected static HonerlawFingerprint from(HashedPeak... hashes) {
			return from(Arrays.asList(hashes));
		}

		protected static HonerlawFingerprint from(List<HashedPeak> hashes) {
			return new HonerlawFingerprint(hashes);
		}

		@Override
		public List<HashedPeak> get() {
			return Collections.unmodifiableList(hashes());
		}

		@Override
		public byte[] getData() {

			byte[] data = NumberUtils.EMPTY_BYTE_ARRAY;

			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				get().stream().map(HashedPeak::getHash).forEach(out::writeBytes);
				data = out.toByteArray();
			}
			catch (IOException ignore) {
				// IOException thrown on ByteArrayOutputStream.close()
			}

			return data;
		}
	}
}
