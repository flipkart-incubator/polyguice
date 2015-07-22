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

/**
 * Factory hook that allows for customizations on components after they have
 * been provisioned by Guice. Polyguice can autodetect ComponentProcessors in
 * the scan package paths and apply them to any components subsequently created.
 * <p>
 *
 * @author indroneel.das
 */

@NonBindable
public interface ComponentProcessor {

/**
 * Apply this processor to the given new component instance after injection of
 * all dependencies. This method will be executed irrespective of the target
 * component having initialization callbacks (like Initializable's
 * <tt>initialize</tt> or a custom init-method).
 * <p>
 *
 * @param component the new component instance.
 */

    void afterInjection(Object component);

/**
 * Apply this processor to the given new component instance before any
 * initialization callbacks (like Initializable's <tt>initialize</tt> or a
 * custom init-method). The component will already be injected with required
 * dependencies.
 * <p>
 *
 * Note that for this method to be executed, the target component must have an
 * initialization callback.
 * <p>
 *
 * @param component the new component instance.
 */

    void beforeInitialization(Object component);

/**
 * Apply this processor to the given new component instance after initialization
 * callbacks (like Initializable's <tt>initialize</tt> or a custom init-method).
 * The component will already be injected with required dependencies.
 * <p>
 *
 * Note that for this method to be executed, the target component must have an
 * initialization callback.
 * <p>
 *
 * @param component the new component instance.
 */

    void afterInitialization(Object component);
}
