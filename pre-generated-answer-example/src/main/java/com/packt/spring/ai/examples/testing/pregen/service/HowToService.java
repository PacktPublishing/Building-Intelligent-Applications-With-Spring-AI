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
package com.packt.spring.ai.examples.testing.pregen.service;

import com.packt.spring.ai.examples.testing.pregen.model.Answer;
import com.packt.spring.ai.examples.testing.pregen.model.Question;

/**
 * Service used to answer questions.
 *
 * @author John Blum
 * @see com.packt.spring.ai.examples.testing.pregen.model.Answer
 * @see com.packt.spring.ai.examples.testing.pregen.model.Question
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface HowToService {

	Answer answer(Question question);

	Answer howTo(Question question);

}
