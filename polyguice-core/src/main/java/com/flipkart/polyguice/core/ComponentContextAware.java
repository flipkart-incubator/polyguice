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
 * Interface to be implemented by any component that wishes to be provided with
 * a common context for lookup of components managed by Guice.
 * <p>
 *
 * Implementing this interface makes sense for example when a component requires
 * access to a set of collaborating components. Note that configuration via
 * injection of component references is preferable to implementing this
 * interface just for lookup purposes.
 * <p>
 *
 * @author indroneel.das
 *
 */

@NonBindable
public interface ComponentContextAware {

/**
 * Set the component context that has a reference to the Guice injector that
 * binds this object. Normally this call will be used to initialize the object.
 * <p>
 *
 * Invoked after population of normal component injections but before an init
 * callback such as {@link Initializable#initialize()} or a custom init-method.
 * <p>
 *
 * @param	compCtxt the context for lookup of components.
 */

    void setComponentContext(ComponentContext compCtxt);
}
