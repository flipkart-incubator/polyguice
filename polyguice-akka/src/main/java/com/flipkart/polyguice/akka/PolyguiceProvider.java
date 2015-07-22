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

package com.flipkart.polyguice.akka;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionId;
import akka.actor.ExtensionIdProvider;

import com.flipkart.polyguice.core.ComponentContext;

/**
 * @author indroneel.das
 *
 */

class PolyguiceProvider extends AbstractExtensionId<PolyguiceExtension>
                                                implements ExtensionIdProvider {

    public static PolyguiceProvider THE_INST;

    private ComponentContext compCtxt;

    PolyguiceProvider(ComponentContext ctxt) {
        compCtxt = ctxt;
        THE_INST = this;
    }

    @Override
    public ExtensionId<PolyguiceExtension> lookup() {
        return PolyguiceProvider.THE_INST;
    }

    @Override
    public PolyguiceExtension createExtension(ExtendedActorSystem system) {
        return new PolyguiceExtension(compCtxt);
    }
}
