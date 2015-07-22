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

package com.flipkart.polyguice.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.ComponentContext;
import com.flipkart.polyguice.core.ConfigurationProvider;
import com.flipkart.polyguice.core.ExternalEntity;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Forms the entry point to the polyguice sub-system.
 * <p>
 *
 * @author indroneel.das
 */

public class Polyguice {

    private static final Logger LOGGER = LoggerFactory.getLogger(Polyguice.class);

    private PolyguiceModule  pgmod;
    private Module[]         xmods;
    private Injector         injector;
    private ComponentContext compCtxt;
    private boolean          prepared;

    public Polyguice() {
        pgmod = new PolyguiceModule();
        prepared = false;
    }

    public Polyguice scanPackage(String name) {
        if(prepared) {
            LOGGER.warn("calling scan package after Guice is prepared. Ignoring.");
            return this;
        }
        pgmod.scanPackage(name);
        return this;
    }

    public Polyguice modules(Module... mods) {
        xmods = mods;
        return this;
    }

    public Polyguice registerExternal(String name, Object value) {
        if(prepared) {
            LOGGER.warn("registering an external entity after Guice is prepared. Ignoring.");
            return this;
        }
        pgmod.registerExternal(name, value);
        return this;
    }

    public Polyguice registerExternal(ExternalEntity entity) {
        if(prepared) {
            LOGGER.warn("registering an external entity after Guice is prepared. Ignoring.");
            return this;
        }
        pgmod.registerExternal(entity.getName(), entity);
        return this;
    }

    public Polyguice registerConfigurationProvider(ConfigurationProvider provider) {
        if(prepared) {
            LOGGER.warn("registering a configuration provider after Guice is prepared. Ignoring.");
            return this;
        }
        pgmod.registerConfigurationProvider(provider);
        return this;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public Polyguice prepare() {
        if(prepared) {
            LOGGER.warn("duplicate call to prepare. Ignoring.");
            return this;
        }
        if(xmods == null || xmods.length == 0) {
            injector = Guice.createInjector(pgmod);
        }
        else {
            Module[] allModules = new Module[xmods.length + 1];
            allModules[0] = pgmod;
            System.arraycopy(xmods, 0, allModules, 1, xmods.length);
            injector = Guice.createInjector(allModules);
        }
        compCtxt = new DefaultComponentContext(injector);
        if(!pgmod.start(compCtxt)) {
            throw new RuntimeException("ployguice failed to prepare");
        }
        prepared = true;
        LOGGER.debug("polyguice prepared");
        return this;
    }

    public ComponentContext getComponentContext() {
        return compCtxt;
    }

    public void stop() {
        System.out.println("stopping polyguice");
        LOGGER.debug("stopping polyguice");
        if(!prepared) {
            LOGGER.warn("not prepared. Nothing to stop.");
            return;
        }
        pgmod.stop();
    }

    public Polyguice registerShutdownHook() {
        Runnable runnable = new Runnable() {
            public void run() {
                pgmod.stop();
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
        return this;
    }
}
