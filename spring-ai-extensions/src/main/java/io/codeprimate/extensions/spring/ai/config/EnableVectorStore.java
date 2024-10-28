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
package io.codeprimate.extensions.spring.ai.config;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Import;

/**
 * Java {@link Annotation} used to import configuraiton used to enable a Spring AI {@link VectorStore}.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.context.annotation.Import
 * @see io.codeprimate.extensions.spring.ai.config.VectorStoreConfiguration
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Import(VectorStoreConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableVectorStore {

}
