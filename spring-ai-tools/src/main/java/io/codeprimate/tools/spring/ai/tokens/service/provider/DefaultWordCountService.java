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
package io.codeprimate.tools.spring.ai.tokens.service.provider;

import io.codeprimate.extensions.util.Utils;
import io.codeprimate.tools.spring.ai.tokens.service.WordCountService;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link WordCountService}.
 *
 * @author John Blum
 * @see io.codeprimate.tools.spring.ai.tokens.service.WordCountService
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@SuppressWarnings("unused")
public class DefaultWordCountService implements WordCountService {

	protected static final String MULTIPLE_SPACES_REGEX_PATTERN = "\\s+";

	@Override
	public int countWords(String content) {

		if (StringUtils.hasText(content)) {
			String singleSpacedContent = content.replaceAll(MULTIPLE_SPACES_REGEX_PATTERN, Utils.SINGLE_SPACE);
			return singleSpacedContent.split(Utils.SINGLE_SPACE).length;
		}

		return 0;
	}
}
