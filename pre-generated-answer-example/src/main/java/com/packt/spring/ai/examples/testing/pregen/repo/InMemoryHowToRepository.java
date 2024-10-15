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
package com.packt.spring.ai.examples.testing.pregen.repo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.packt.spring.ai.examples.testing.pregen.model.HowTo;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link Repository} used to store {@link HowTo} objects in-memory.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregen.model.HowTo
 * @see org.springframework.stereotype.Repository
 * @since 1.0.0
 */
@Repository
@Getter(AccessLevel.PROTECTED)
public class InMemoryHowToRepository implements HowToRepository {

	private final Set<HowTo> howTos = Collections.synchronizedSet(new HashSet<>());

	@Override
	public @NonNull Iterator<HowTo> iterator() {
		return Collections.unmodifiableSet(getHowTos()).iterator();
	}

	@Override
	public boolean save(HowTo howTo) {
		Assert.notNull(howTo, "HowTo is required");
		return getHowTos().add(howTo);
	}
}
