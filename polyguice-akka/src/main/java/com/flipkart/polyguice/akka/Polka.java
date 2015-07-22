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

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;

import com.flipkart.polyguice.core.Bindable;

/**
 * @author indroneel.das
 *
 */

@Bindable
public interface Polka {

    String KEY_ACTOR_SYSTEM = "key.actorsystem";

    ActorRef getActor(String name);

    ActorRef getActor(String name, Props props);

    void removeActor(String name);

    Cancellable schedule(long initialDelay, long interval, String actorName, Object message);

    Cancellable runOnce(long initialDelay, String actorName, Object message);
}
