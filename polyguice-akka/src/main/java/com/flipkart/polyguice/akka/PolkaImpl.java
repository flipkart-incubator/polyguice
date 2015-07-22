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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Scheduler;

import com.flipkart.polyguice.core.Component;
import com.flipkart.polyguice.core.ComponentContext;
import com.flipkart.polyguice.core.ComponentContextAware;
import com.flipkart.polyguice.core.External;
import com.flipkart.polyguice.core.Initializable;

/**
 * The default implementation of Polka as a polyguice component. After
 * initialization, maintains an actor system that is setup with specific
 * extensions.
 * <p/>
 *
 * @author indroneel.das
 */

@Component
@Singleton
public class PolkaImpl implements ComponentContextAware, Initializable, Polka {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolkaImpl.class);

    private ComponentContext compCtxt;

    @External(name = KEY_ACTOR_SYSTEM, required = true)
    private ActorSystem actorSystem;

    private Map<String, ActorRef> actorMap = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface ComponentContextAware

    @Override
    public void setComponentContext(ComponentContext ctxt) {
        compCtxt = ctxt;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface Initializable

    @Override
    public void initialize() {
        actorSystem.registerExtension(new PolyguiceProvider(compCtxt));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface Polka

    @Override
    public ActorRef getActor(String name) {
        if (actorMap.containsKey(name)) {
            return actorMap.get(name);
        }
        return null;
    }

    @Override
    public ActorRef getActor(String name, Props props) {
        if (actorMap.containsKey(name)) {
            return actorMap.get(name);
        }
        LOGGER.info("Creating new actor|Name={}|Props={}", name, props);
        ActorRef actor = actorSystem.actorOf(props, name);
        actorMap.put(name, actor);
        return actor;
    }

    @Override
    public void removeActor(String name) {
        if (!actorMap.containsKey(name)) {
            return;
        }
        LOGGER.info("Removing actor|Name={}", name);
        ActorRef ref = actorMap.get(name);
        actorSystem.stop(ref);
        actorMap.remove(name);
    }

    @Override
    public Cancellable schedule(long initialDelay, long interval,
                                String actorName, Object message) {

        ActorRef actor = getActor(actorName);
        if (actor == null) {
            return null;
        }
        FiniteDuration initial = new FiniteDuration(initialDelay, TimeUnit.MILLISECONDS);
        FiniteDuration gap = new FiniteDuration(interval, TimeUnit.MILLISECONDS);
        Scheduler scheduler = actorSystem.scheduler();
        return scheduler.schedule(initial, gap, actor, message, actorSystem.dispatcher(), ActorRef.noSender());
    }

    @Override
    public Cancellable runOnce(long initialDelay, String actorName,
                               Object message) {

        ActorRef actor = getActor(actorName);
        if (actor == null) {
            return null;
        }
        FiniteDuration initial = new FiniteDuration(initialDelay, TimeUnit.MILLISECONDS);
        Scheduler scheduler = actorSystem.scheduler();
        return scheduler.scheduleOnce(initial, actor, message, actorSystem.dispatcher(), ActorRef.noSender());
    }
}
