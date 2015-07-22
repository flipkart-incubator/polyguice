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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

/**
 * The base class for all untyped actors that for automatic and transparent
 * dependency injection using polyguice. These actors need not be created
 * through Polka. However, an instance of Polka must be created and managed by
 * polyguice separately, at the time of actor creation.
 * <p>
 *
 * @author  indroneel.das
 */

public abstract class InjectedUntypedActor extends UntypedActor {

    @Override
    public final void preStart() throws Exception {
        PolyguiceExtension ext = PolyguiceProvider.THE_INST.get(getContext().system());
        ext.inject(this);
        doStart();
    }

    @Override
    public final void postRestart(Throwable reason) throws Exception {
        // Create the logger instance here with the class obtained polymorphically
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("Actor Restarted", reason);

        PolyguiceExtension ext = PolyguiceProvider.THE_INST.get(getContext().system());
        ext.inject(this);
        super.postRestart(reason);
        doStart();
    }

/**
 * This is a template method that is provided in case you wish to perform some
 * custom operations at the time of actor start or restart.
 * <p>
 *
 * @throws Exception
 */

    protected void doStart() throws Exception {
        //NOOP
    }
}
