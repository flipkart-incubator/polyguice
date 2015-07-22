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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.External;

/**
 * @author indroneel.das
 *
 */

class ExternalsInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalsInjector.class);

    private Map<String, Object> externals;

    ExternalsInjector() {
        externals = new HashMap<>();
    }

    public void register(String name, Object value) {
        externals.put(name, value);
        LOGGER.debug("registered external: {}", name);
    }

    public void injectComponent(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        for(Field field : fields) {
            if(!injectField(target, field)) {
                throw new RuntimeException("external injection failed on field "
                        + target.getClass().getName() + "#" + field.getName());
            }
        }
        Method[] methods = target.getClass().getMethods();
        for(Method method : methods) {
            if(!injectMethod(target, method)) {
                throw new RuntimeException("external injection failed on method "
                        + target.getClass().getName() + "#" + method.getName());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private boolean injectField(Object target, Field field) {
        External ann = field.getAnnotation(External.class);
        if(ann == null) {
            return true;
        }
        LOGGER.debug("field {}#{} has {}", field.getDeclaringClass().getName(), field.getName(), ann.toString());
        boolean required = ann.required();
        String extName = ann.name();
        if(extName == null || extName.trim().length() == 0) {
            extName = ann.value();
            if(extName == null || extName.trim().length() == 0) {
                LOGGER.warn("InjectExternal on: {} does not have a name", field.getName());
                return !required; //return true only if not required.
            }
        }

        Object value = externals.get(extName);
        if(value == null) {
            LOGGER.warn("external variable: {} not found", extName);
            return !required; //return true only if not required.
        }

        if(!field.getType().isAssignableFrom(value.getClass())) {
            LOGGER.warn("external variable: {} type mismatch", extName);
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
            LOGGER.warn("global variable: {} error injecting", extName);
            return !required; //return true only if not required.
        }
        return true;
    }

    private boolean injectMethod(Object target, Method method) {
        External ann = method.getAnnotation(External.class);
        if(ann == null) {
            return true;
        }
        LOGGER.debug("method {}#{} has {}", method.getDeclaringClass().getName(), method.getName(), ann.toString());
        boolean required = ann.required();
        String extName = ann.name();
        if(extName == null || extName.trim().length() == 0) {
            extName = ann.value();
            if(extName == null || extName.trim().length() == 0) {
                return !required; //return true only if not required.
            }
        }
        Object value = externals.get(extName);
        if(value == null) {
            LOGGER.warn("external variable: {} not found", extName);
            return !required; //return true only if not required.
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

        if(!paramTypes[0].isAssignableFrom(value.getClass())) {
            LOGGER.warn("method {}#{} type mismtach with global variable {}",
                    method.getDeclaringClass().getName(), method.getName(), extName);
            return !required; //return true only if not required.
        }

        try {
            method.invoke(target, value);
        }
        catch(Exception exep) {
            LOGGER.warn("method {}#{} error injecting",
                    method.getDeclaringClass().getName(), method.getName());
            return !required; //return true only if not required.
        }
        return true;
    }
}
