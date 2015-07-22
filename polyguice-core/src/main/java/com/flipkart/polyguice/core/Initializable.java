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
 * <p>
 * Interface to be implemented by components that need to react once all their
 * properties have been set by Guice: for example, to perform custom
 * initialization, or merely to check that all mandatory properties have been
 * set.
 * </p>
 *
 * @author indroneel.das
 */

@NonBindable
public interface Initializable {

/**
 * <p>
 * Invoked by Polyguice after the corresponding Guice container has injected all
 * required dependencies on this component.
 * </p>
 */

    void initialize();
}
