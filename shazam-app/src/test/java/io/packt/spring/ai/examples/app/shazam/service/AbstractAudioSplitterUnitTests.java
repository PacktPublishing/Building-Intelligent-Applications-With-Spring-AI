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

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit Tests for {@link AbstractAudioSplitter}
 *
 * @author John Blum
 * @see AbstractAudioSplitter
 * @see org.junit.jupiter.api.Test
 * @see org.mockito.Mock
 * @see org.mockito.junit.jupiter.MockitoExtension
 * @since 0.1.0
 */
@ExtendWith(MockitoExtension.class)
public class AbstractAudioSplitterUnitTests {

	@Mock
	private AudioFormat mockAudioFormat;

	private byte asByte(int integer) {
		return (byte) integer;
	}

	@Test
	void audioClipHalfEven() {

		byte[] audioData = {
			asByte(0xC), asByte(0xA), asByte(0xF), asByte(0xE), asByte(0xB), asByte(0xA), asByte(0xB), asByte(0xE)
		};

		AbstractAudioSplitter.AudioClip audioClip =
			AbstractAudioSplitter.AudioClip.from(audioData, this.mockAudioFormat);

		assertThat(audioClip).isNotNull();
		assertThat(audioClip.getData()).hasSameSizeAs(audioData);

		AbstractAudioSplitter.AudioClip firstHalf = audioClip.firstHalf();

		assertThat(firstHalf).isNotNull();
		assertThat(firstHalf).isNotSameAs(audioClip);
		assertThat(firstHalf.size()).isEqualTo(4);
		assertThat(firstHalf.getData()).containsExactly(asByte(0xC), asByte(0xA), asByte(0xF), asByte(0xE));

		AbstractAudioSplitter.AudioClip secondHalf = audioClip.secondHalf();

		assertThat(secondHalf).isNotNull();
		assertThat(secondHalf).isNotSameAs(audioClip).isNotSameAs(firstHalf);
		assertThat(secondHalf.size()).isEqualTo(4);
		assertThat(secondHalf.getData()).containsExactly(asByte(0xB), asByte(0xA), asByte(0xB), asByte(0xE));

		AbstractAudioSplitter.AudioClip bothHalves = firstHalf.merge(secondHalf);

		assertThat(bothHalves).isNotNull();
		assertThat(bothHalves).isNotSameAs(audioClip).isNotSameAs(firstHalf).isNotSameAs(secondHalf);
		assertThat(bothHalves.getData()).hasSameSizeAs(audioData);
		assertThat(Arrays.equals(bothHalves.getAudio().getData(), audioData)).isTrue();
	}

	@Test
	void audioClipHalfOdd() {

		byte[] audioData = {
			asByte(0xD), asByte(0xE), asByte(0xC), asByte(0xA), asByte(0xF)
		};

		AbstractAudioSplitter.AudioClip audioClip =
			AbstractAudioSplitter.AudioClip.from(audioData, this.mockAudioFormat);

		assertThat(audioClip).isNotNull();
		assertThat(audioClip.getData()).hasSameSizeAs(audioData);

		AbstractAudioSplitter.AudioClip firstHalf = audioClip.firstHalf();

		assertThat(firstHalf).isNotNull();
		assertThat(firstHalf).isNotSameAs(audioClip);
		assertThat(firstHalf.size()).isEqualTo(2);
		assertThat(firstHalf.getData()).containsExactly(asByte(0xD), asByte(0xE));

		AbstractAudioSplitter.AudioClip secondHalf = audioClip.secondHalf();

		assertThat(secondHalf).isNotNull();
		assertThat(secondHalf).isNotSameAs(audioClip).isNotSameAs(firstHalf);
		assertThat(secondHalf.size()).isEqualTo(3);
		assertThat(secondHalf.getData()).containsExactly(asByte(0xC), asByte(0xA), asByte(0xF));

		AbstractAudioSplitter.AudioClip bothHalves = firstHalf.merge(secondHalf);

		assertThat(bothHalves).isNotNull();
		assertThat(bothHalves).isNotSameAs(audioClip).isNotSameAs(firstHalf).isNotSameAs(secondHalf);
		assertThat(bothHalves.getData()).hasSameSizeAs(audioData);
		assertThat(Arrays.equals(bothHalves.getAudio().getData(), audioData)).isTrue();
	}
}
