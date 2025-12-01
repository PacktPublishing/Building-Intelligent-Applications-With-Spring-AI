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
package io.packt.spring.ai.examples.app.shazam.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Spring Web MVC {@link ControllerAdvice} used to process and handle common Shazam {@link Controller} requests.
 *
 * @author John Blum
 * @see Controller
 * @see ControllerAdvice
 * @since 0.1.0
 */
@ControllerAdvice
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class ShazamControllerAdvice {

	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxUploadFileSize;

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Object> handleMaxFileSizeUploadError(MaxUploadSizeExceededException cause, Model model) {

		String message = "Exceeded max file size [%s] on upload: %s"
			.formatted(getMaxUploadFileSize(), cause.getMessage());

		model.addAttribute("uploadMessage", message);

		return ResponseEntity.badRequest().build();
	}
}
