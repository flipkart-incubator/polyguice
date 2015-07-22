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

import java.util.ArrayList;
import java.util.List;

import com.flipkart.polyguice.core.ComponentContext;
import com.flipkart.polyguice.core.ConfigurationProvider;
import com.google.inject.AbstractModule;

public class PolyguiceModule extends AbstractModule {

    private List<String>          scanPkgNames;
    private LifecycleManager      lifeMan;
    private ExternalsInjector     externInject;
    private ConfigurationInjector confInject;

    PolyguiceModule() {
        scanPkgNames = new ArrayList<>();
        externInject = new ExternalsInjector();
        confInject = new ConfigurationInjector();
    }

    public void scanPackage(String name) {
        scanPkgNames.add(name);
    }

    public void registerExternal(String name, Object value) {
        externInject.register(name, value);
    }

    public void registerConfigurationProvider(ConfigurationProvider provider) {
        confInject.register(provider);
    }

    public boolean start(ComponentContext compCtxt) {
        lifeMan.setComponentContext(compCtxt);
        return lifeMan.start();
    }

    public void stop() {
        lifeMan.stop();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of base class AbstractModule

    @Override
    protected void configure() {
        AutobindManager abm = new AutobindManager(binder());

        String[] pkgNamesArr = scanPkgNames.toArray(new String[scanPkgNames.size()]);
        abm.autobind(pkgNamesArr);

        lifeMan = new LifecycleManager(binder());
        lifeMan.setExternalsInjector(externInject);
        lifeMan.setConfigurationInjector(confInject);
        lifeMan.setProcessors(abm.getComponentProcessors());
        lifeMan.setSingletons(abm.getSingletons());
    }
}
