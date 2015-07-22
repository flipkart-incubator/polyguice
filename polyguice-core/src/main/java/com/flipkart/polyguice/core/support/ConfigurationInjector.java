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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.Configuration;
import com.flipkart.polyguice.core.ConfigurationProvider;

/**
 * @author indroneel.das
 *
 */

class ConfigurationInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationInjector.class);

    private List<ConfigurationProvider> configProviders;

    public ConfigurationInjector() {
        configProviders = new ArrayList<>();
    }

    public void register(ConfigurationProvider provider) {
        configProviders.add(provider);
        LOGGER.debug("registered configuration provider: {}", provider.getClass().getName());
    }

    public void injectComponent(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        for(Field field : fields) {
            if(!injectField(target, field)) {
                throw new RuntimeException("preference injection failed on field "
                        + target.getClass().getName() + "#" + field.getName());
            }
        }
        Method[] methods = target.getClass().getMethods();
        for(Method method : methods) {
            if(!injectMethod(target, method)) {
                throw new RuntimeException("preference injection failed on method "
                        + target.getClass().getName() + "#" + method.getName());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private boolean injectField(Object target, Field field) {
        Configuration ann = field.getAnnotation(Configuration.class);
        if(ann == null) {
            return true;
        }
        LOGGER.debug("field {}#{} has {}", field.getDeclaringClass().getName(), field.getName(), ann.toString());

        boolean required = ann.required();
        String key = ann.name();
        if(key == null || key.trim().length() == 0) {
            key = ann.value();
            if(key == null || key.trim().length() == 0) {
                LOGGER.warn("InjectPreference on: {}#{} does not have a name",
                        field.getDeclaringClass().getName(), field.getName());
                return !required; //return true only if not required.
            }
        }

        Object value = retrieveConfig(key, field.getType());
        if(value == null) {
            LOGGER.warn("preference {} not found or not of required type", key);
            return !required; //return true only if not required.
        }

        try {
            boolean accessible = field.isAccessible();
            if(!accessible) {
                field.setAccessible(true);
            }
            field.set(target, value);
            if(!accessible) {
                field.setAccessible(false);
            }
        }
        catch(Exception exep) {
            LOGGER.warn("preference: {} error injecting on field {}#{}",
                    key, field.getDeclaringClass().getName(), field.getName());
            return !required; //return true only if not required.
        }
        return true;
    }

    private boolean injectMethod(Object target, Method method) {
        Configuration ann = method.getAnnotation(Configuration.class);
        if(ann == null) {
            return true;
        }
        LOGGER.debug("method {}#{} has {}", method.getDeclaringClass().getName(), method.getName(), ann.toString());

        boolean required = ann.required();
        String key = ann.name();
        if(key == null || key.trim().length() == 0) {
            key = ann.value();
            if(key == null || key.trim().length() == 0) {
                LOGGER.warn("InjectPreference on: {}#{} does not have a name",
                        method.getDeclaringClass().getName(), method.getName());
                return !required; //return true only if not required.
            }
        }

        int mod = method.getModifiers();
        if(Modifier.isAbstract(mod) || !Modifier.isPublic(mod)) {
            LOGGER.warn("method {}#{} must be public and not abstract",
                    method.getDeclaringClass().getName(), method.getName());
            return !required; //return true only if not required.
        }

        if(method.getReturnType() != Void.TYPE) {
            LOGGER.warn("method {}#{} must not have a return type",
                    method.getDeclaringClass().getName(), method.getName());
            return !required; //return true only if not required.
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        if(paramTypes.length != 1) {
            LOGGER.warn("method {}#{} must have exactly one parameter",
                    method.getDeclaringClass().getName(), method.getName());
            return !required; //return true only if not required.
        }

        Object value = retrieveConfig(key, method.getParameterTypes()[0]);
        if(value == null) {
            LOGGER.warn("preference {} not found or not of required type", key);
            return !required; //return true only if not required.
        }

        try {
            method.invoke(target, value);
        }
        catch(Exception exep) {
            LOGGER.warn("preference: {} error injecting on method {}#{}",
                    key, method.getDeclaringClass().getName(), method.getName());
            return !required; //return true only if not required.
        }
        return true;
    }

    private Object retrieveConfig(String name, Class<?> type) {

        for(ConfigurationProvider provider : configProviders) {
            if(!provider.contains(name)) {
                continue;
            }
            Object value = provider.getValue(name, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
