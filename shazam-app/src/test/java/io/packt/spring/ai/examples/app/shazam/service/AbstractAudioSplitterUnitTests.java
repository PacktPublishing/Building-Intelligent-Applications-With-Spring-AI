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
package io.packt.spring.ai.examples.app.shazam.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link AbstractAudioSplitter}
 *
 * @author John Blum
 * @see AbstractAudioSplitter
 * @see org.junit.jupiter.api.Test
 * @since 0.1.0
 */
public class AbstractAudioSplitterUnitTests {

	private byte asByte(int integer) {
		return (byte) integer;
	}

	@Test
	void audioClipHalfEven() {

		byte[] audioData = {
			asByte(0xC), asByte(0xA), asByte(0xF), asByte(0xE), asByte(0xB), asByte(0xA), asByte(0xB), asByte(0xE)
		};

		AbstractAudioSplitter.AudioClip audioClip = AbstractAudioSplitter.AudioClip.from(audioData);

		assertThat(audioClip).isNotNull();
		assertThat(audioClip.size()).isEqualTo(audioData.length);

		AbstractAudioSplitter.AudioClip firstHalf = audioClip.firstHalf();

		assertThat(firstHalf).isNotNull();
		assertThat(firstHalf.size()).isEqualTo(4);
		assertThat(firstHalf.data()).contains(asByte(0xC), asByte(0xA), asByte(0xF), asByte(0xE));

		AbstractAudioSplitter.AudioClip secondHalf = audioClip.secondHalf();

		assertThat(secondHalf).isNotNull();
		assertThat(secondHalf.size()).isEqualTo(4);
		assertThat(secondHalf.data()).contains(asByte(0xB), asByte(0xA), asByte(0xB), asByte(0xE));

		AbstractAudioSplitter.AudioClip bothHalves = firstHalf.merge(secondHalf);

		assertThat(bothHalves).isNotNull();
		assertThat(bothHalves.size()).isEqualTo(audioData.length);
		assertThat(bothHalves.data()).contains(audioData);
	}

	@Test
	void audioClipHalfOdd() {

		byte[] audioData = {
			asByte(0xD), asByte(0xE), asByte(0xC), asByte(0xA), asByte(0xF)
		};

		AbstractAudioSplitter.AudioClip audioClip = AbstractAudioSplitter.AudioClip.from(audioData);

		assertThat(audioClip).isNotNull();
		assertThat(audioClip.size()).isEqualTo(audioData.length);

		AbstractAudioSplitter.AudioClip firstHalf = audioClip.firstHalf();

		assertThat(firstHalf).isNotNull();
		assertThat(firstHalf.size()).isEqualTo(2);
		assertThat(firstHalf.data()).contains(asByte(0xD), asByte(0xE));

		AbstractAudioSplitter.AudioClip secondHalf = audioClip.secondHalf();

		assertThat(secondHalf).isNotNull();
		assertThat(secondHalf.size()).isEqualTo(3);
		assertThat(secondHalf.data()).contains(asByte(0xC), asByte(0xA), asByte(0xF));

		AbstractAudioSplitter.AudioClip bothHalves = firstHalf.merge(secondHalf);

		assertThat(bothHalves).isNotNull();
		assertThat(bothHalves.size()).isEqualTo(audioData.length);
		assertThat(bothHalves.data()).contains(audioData);
	}
}
