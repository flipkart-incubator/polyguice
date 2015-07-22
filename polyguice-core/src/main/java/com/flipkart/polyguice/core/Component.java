/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.flipkart.polyguice.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.name.Named;

/**
 * When applied to a class, indicates that the concrete type should be
 * provisioned as a concrete implementation bound to one or more interfaces that
 * are annotated with {@link Bindable}.
 * <p>
 *
 * @author indroneel.das
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Component {

/**
 * Provides a name such that the component is also bound using {@link Named}.
 * This is an optional information, if not provided, no named style binding is
 * done.
 *
 * @return	a name for this component.
 */

    String value() default "";

/**
 * Same as {@link #value()}. This is used to provide the binding name in cases
 * where a <tt>strictBinding</tt> needs to be explicitly provided.
 * <p>
 *
 * @return	a name for this component.
 */

    String name() default "";

    boolean namedOnly() default false;
}
