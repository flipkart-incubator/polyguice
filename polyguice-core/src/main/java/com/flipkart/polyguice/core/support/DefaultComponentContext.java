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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.ComponentContext;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * @author indroneel.das
 *
 */

class DefaultComponentContext implements ComponentContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentContext.class);

    private Injector injector;

    DefaultComponentContext(Injector injector) {
        this.injector = injector;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface ComponentContext

    @Override
    public Object getInstance(String name) {
        try {
            return injector.getInstance(Key.get(Object.class, Names.named(name)));
        }
        catch(Exception exep) {
            LOGGER.warn(exep.getMessage());
        }
        return null;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        try {
            return injector.getInstance(type);
        }
        catch(Exception exep) {
            LOGGER.warn(exep.getMessage());
        }
        return null;
    }

    @Override
    public <T> T getInstance(String name, Class<T> type) {
        try {
            return injector.getInstance(Key.get(type, Names.named(name)));
        }
        catch(Exception exep) {
            LOGGER.warn(exep.getMessage());
        }
        return null;
    }

    @Override
    public <T> List<T> getInstances(Class<T> type) {
        List<T> instances = new ArrayList<T>();
        List<Binding<T>> bindings = injector.findBindingsByType(TypeLiteral.get(type));
        for(Binding<T> binding : bindings) {
            Key<T> key = binding.getKey();
            instances.add(injector.getInstance(key));
        }
        return instances;
    }

    @Override
    public void inject(Object target) {
        injector.injectMembers(target);
    }
}
