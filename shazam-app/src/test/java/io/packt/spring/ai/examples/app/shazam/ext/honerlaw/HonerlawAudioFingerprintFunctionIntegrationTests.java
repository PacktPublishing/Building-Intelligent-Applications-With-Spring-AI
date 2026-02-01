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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;

import io.honerlaw.audio.fingerprint.hash.peak.HashedPeak;
import io.packt.spring.ai.examples.app.shazam.AbstractShazamIntegrationTests;
import io.packt.spring.ai.examples.app.shazam.dsp.AudioFingerprintFunction;
import io.packt.spring.ai.examples.app.shazam.dsp.Fingerprint;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.AudioFormatBuilder;
import io.packt.spring.ai.examples.app.shazam.ext.javax.sound.sample.ShazamAudioFormat;
import io.packt.spring.ai.examples.app.shazam.model.Audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Integration Tests for {@link HonerlawAudioFingerprintFunction}.
 *
 * @author John Blum
 * @see HonerlawAudioFingerprintFunction
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class HonerlawAudioFingerprintFunctionIntegrationTests extends AbstractShazamIntegrationTests {

	private static final boolean DEBUG = false;

	private static final String RESOURCE_PATH = "PearlJam-Ten-Jeremy.wav";

	private final AudioFingerprintFunction<List<HashedPeak>> audioFingerprintFunction =
		new HonerlawAudioFingerprintFunction();

	@Test
	@EnabledIf("resourceExists")
	void audioFingerprint() {

		Audio audio = Audio.from(resource());
		Fingerprint<List<HashedPeak>> audioFingerprint = this.audioFingerprintFunction.compute(audio);
		List<HashedPeak> hashes = assertFingerprint(audioFingerprint);

		logHashes(hashes);

		assertHashedPeaks(audio, hashes);
	}

	List<HashedPeak> assertFingerprint(Fingerprint<List<HashedPeak>> fingerprint) {

		assertThat(fingerprint).isNotNull();

		List<HashedPeak> hashes = fingerprint.get();

		assertThat(hashes).isNotNull();
		assertThat(hashes).hasSizeGreaterThan(0);
		assertThat(hashes.stream().map(HashedPeak::getHashAsHex).collect(Collectors.toSet()))
			.hasSizeLessThanOrEqualTo(hashes.size());

		return hashes;
	}

	List<HashedPeak> assertHashedPeaks(Audio audio, List<HashedPeak> hashes) {

		AudioFormat audioFormat =  AudioFormatBuilder.from(audio).build();

		Duration audioDuration =  audioFormat instanceof ShazamAudioFormat shazamAudioFormat
			? shazamAudioFormat.getDuration()
			: Duration.ZERO;

		AtomicInteger index = new AtomicInteger(0);

		long deltaSum = hashes.stream()
			.map(peak -> {
				assertThat(peak.getPeakOne().getTime())
					.describedAs("Iteration [%d] of [%d]", index.incrementAndGet(), hashes.size())
					.isLessThanOrEqualTo(peak.getPeakTwo().getTime());
				return peak.getDelta();
			})
			.reduce(Integer::sum)
			.orElse(0);

		Duration duration = Duration.ofMillis(deltaSum);

		assertThat(duration).isGreaterThanOrEqualTo(audioDuration); // TODO: ???

		return hashes;
	}

	List<HashedPeak> logHashes(List<HashedPeak> hashes) {

		if (DEBUG) {
			AtomicLong counter = new AtomicLong(0L);

			hashes.forEach(hash -> {
				byte[] hashData = hash.getHash();
				log("HASH[%s] is [%s] with byte array of size [%d] and data [%s]%n",
					counter.incrementAndGet(), hash.getHashAsHex(), hashData.length, Arrays.toString(hashData));
			});
		}

		return hashes;
	}

	@Override
	protected String resourcePath() {
		return RESOURCE_PATH;
	}
}
