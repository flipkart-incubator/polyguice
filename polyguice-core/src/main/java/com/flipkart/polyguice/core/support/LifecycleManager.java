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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.ComponentContext;
import com.flipkart.polyguice.core.ComponentContextAware;
import com.flipkart.polyguice.core.ComponentProcessor;
import com.flipkart.polyguice.core.Disposable;
import com.flipkart.polyguice.core.InitMethod;
import com.flipkart.polyguice.core.Initializable;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 *
 * @author indroneel.das
 *
 */

class LifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);

    private List<String>             procNames;
    private List<SingletonKey>       singletonKeys;
    private ComponentContext         compCtxt;
    private ExternalsInjector        externInject;
    private ConfigurationInjector    confInject;
    private List<ComponentProcessor> processors;
    private List<Disposable>         disposables;

    private boolean startupError;

    LifecycleManager(Binder binder) {
        binder.bindListener(new ProvisionMatcher(), new ProvisionHandler());
        binder.bindListener(new InjectionMatcher(), new InjectionTypeHandler());
        procNames = new ArrayList<>();
        singletonKeys = new ArrayList<>();
        disposables = new ArrayList<>();
    }

    public void setProcessors(List<String> names) {
        procNames.addAll(names);
    }

    public void setSingletons(List<SingletonKey> keys) {
        singletonKeys.addAll(keys);
    }

    public void setComponentContext(ComponentContext ctxt) {
        compCtxt = ctxt;
    }

    public void setExternalsInjector(ExternalsInjector inject) {
        externInject = inject;
    }

    public void setConfigurationInjector(ConfigurationInjector inject) {
        confInject = inject;
    }

    public boolean start() {
        LOGGER.debug("starting lifecycle operations");
        startupError = false;
        processors = new ArrayList<>();
        for(String name : procNames) {
            ComponentProcessor proc = (ComponentProcessor)
                    compCtxt.getInstance(name);
            if(proc != null) {
                processors.add(proc);
                LOGGER.debug("loaded component processor {} => {}", name, proc.getClass().getName());
            }
            else {
                LOGGER.error("failed to load component processor: {}", name);
                return false;
            }
        }

        for(SingletonKey key : singletonKeys) {
            Object ston = key.loadComponent(compCtxt);
            if(ston == null) {
                LOGGER.error("failed to load singleton: {}", key);
                return false;
            }
            if(ston instanceof Disposable) {
                disposables.add((Disposable) ston);
            }
        }
        return !startupError;
    }

    public void stop() {
        for(Disposable disp : disposables) {
            LOGGER.debug("disposing {}", disp.getClass().getName());
            try {
                disp.dispose();
            }
            catch(Exception exep) {
                LOGGER.error("while disposing " + disp.getClass().getName(), exep);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private void doAfterInjection(Object component) {
        if(component instanceof ComponentContextAware) {
            LOGGER.debug("component is context aware. Injecting context.");
            ((ComponentContextAware) component).setComponentContext(compCtxt);
        }

        LOGGER.debug("injecting configuration objects on {}", component.getClass().getName());
        confInject.injectComponent(component);

        LOGGER.debug("injecting external objects on {}", component.getClass().getName());
        externInject.injectComponent(component);

        for(ComponentProcessor proc : processors) {
            LOGGER.debug("component processor {} => after injection", proc.getClass().getName());
            proc.afterInjection(component);
        }
    }

    private void doInitialization(Object component) {
        if(component instanceof Initializable) {
            for(ComponentProcessor proc : processors) {
                proc.beforeInitialization(component);
            }
            ((Initializable) component).initialize();
            for(ComponentProcessor proc : processors) {
                proc.afterInitialization(component);
            }
        }
        else {
            Method initMthd = findInitMethod(component.getClass());
            if(initMthd != null) {
                for(ComponentProcessor proc : processors) {
                    proc.beforeInitialization(component);
                }
                try {
                    initMthd.invoke(component, new Object[]{});
                }
                catch (Exception exep) {
                    throw new RuntimeException("error executing init method", exep);
                }
                for(ComponentProcessor proc : processors) {
                    proc.afterInitialization(component);
                }
            }
        }
    }

    private Method findInitMethod(Class<?> cls) {
        Method[] methods = cls.getMethods();
        for(Method method : methods) {
            if(method.getAnnotation(InitMethod.class) != null) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if(paramTypes.length == 0 && method.getReturnType() == Void.TYPE) {
                    return method;
                }
                else {
                    LOGGER.warn("init-method: {} should have zero args and no return type", method.getName());
                }
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private class ProvisionMatcher extends AbstractMatcher<Binding<?>> {

        @Override
        public boolean matches(Binding<?> binding) {
            return true;
        }
    }

    private class ProvisionHandler implements ProvisionListener {

        @Override
        public <T> void onProvision(ProvisionInvocation<T> provision) {
            T component = provision.provision();
            try {
                doInitialization(component);
                LOGGER.debug("provisioned {} -> {}", component.getClass().getName(), component);
            }
            catch(Exception exep) {
                LOGGER.error(exep.toString(), exep);
                startupError = true;
                throw exep;
            }
        }
    }

    private class InjectionMatcher extends AbstractMatcher<TypeLiteral<?>> {

        @Override
        public boolean matches(TypeLiteral<?> tl) {
            return true;
        }
    }

    private class InjectionTypeHandler implements TypeListener {

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new InjectionHandler());
        }
    }

    private class InjectionHandler implements InjectionListener<Object> {

        @Override
        public void afterInjection(Object component) {
            LOGGER.debug("injections complete on {}", component.getClass().getName());
            try {
                doAfterInjection(component);
            }
            catch(Exception exep) {
                LOGGER.error(exep.toString(), exep);
                startupError = true;
                throw exep;
            }
        }
    }
}
