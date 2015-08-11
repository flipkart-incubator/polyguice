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

package com.flipkart.polyguice.dropwiz;

import io.dropwizard.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.polyguice.core.ConfigurationProvider;

/**
 * @author indroneel.das
 *
 */

public class DropConfigProvider implements ConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropConfigProvider.class);

    private Configuration dwConfig;

    public DropConfigProvider(Configuration config) {
        dwConfig = config;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface ConfigurationProvider

    @Override
    public boolean contains(String path) {
        LOGGER.debug("checking for configuration {}", path);
        try {
            return (getValueRecursive(path, dwConfig) != null);
        }
        catch (Exception exep) {
            //NOOP
        }
        return false;
    }

    @Override
    public Object getValue(String path, Class<?> type) {
        Object value = null;
        try {
            value = getValueRecursive(path, dwConfig);
        }
        catch (Exception exep) {
            return null;
        }
        if(value == null) {
            return null;
        }

        if(type.isAssignableFrom(value.getClass())) {
            return value;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private Object getValueRecursive(String path, Object inst) throws Exception {
        String[] parts = path.split("\\.", 2);
        Object value = getValueFromFields(parts[0], inst.getClass(), inst);
        if(value == null) {
            value = getValueFromMethods(parts[0], inst.getClass(), inst);
        }
        if(value != null) {
            if(parts.length > 1) {
                return getValueRecursive(parts[1], value);
            }
            else {
                return value;
            }
        }
        return null;
    }

    private Object getValueFromFields(String path, Class<?> type, Object inst) throws Exception {
        Field[] fields = type.getDeclaredFields();
        for(Field field : fields) {
            JsonProperty ann = field.getAnnotation(JsonProperty.class);
            if(ann != null) {
                String annName = ann.value();
                if(StringUtils.isBlank(annName)) {
                    annName = ann.defaultValue();
                }
                if(StringUtils.isBlank(annName)) {
                    annName = field.getName();
                }
                if(StringUtils.equals(path, annName)) {
                    boolean accessible = field.isAccessible();
                    if(!accessible) {
                        field.setAccessible(true);
                    }
                    Object value = field.get(inst);
                    if(!accessible) {
                        field.setAccessible(false);
                    }
                    return value;
                }
            }
        }
        return null;
    }

    private Object getValueFromMethods(String path, Class<?> type, Object inst) throws Exception {
        Method[] methods = type.getDeclaredMethods();
        for(Method method : methods) {
            JsonProperty ann = method.getAnnotation(JsonProperty.class);
            if(ann != null) {
                String annName = ann.value();
                if(StringUtils.isBlank(annName)) {
                    annName = ann.defaultValue();
                }
                if(StringUtils.isBlank(annName)) {
                    annName = getNameFromMethod(method);
                }
                if(StringUtils.equals(path, annName)) {
                    boolean accessible = method.isAccessible();
                    if(!accessible) {
                        method.setAccessible(true);
                    }
                    Object value = method.invoke(inst);
                    if(!accessible) {
                        method.setAccessible(false);
                    }
                    return value;
                }
            }
        }
        return null;
    }

    private String getNameFromMethod(Method method) {
        if(method.getParameterCount() > 0) {
            return null;
        }
        if(method.getReturnType().equals(Void.TYPE)) {
            return null;
        }

        String mthdName = method.getName();
        if(mthdName.startsWith("get")) {
            if(mthdName.length() <= 3) {
                return null;
            }
            if(method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(Boolean.TYPE)) {
                return null;
            }
            StringBuffer buffer = new StringBuffer(StringUtils.removeStart(mthdName, "get"));
            buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
            return buffer.toString();
        }
        else if(!mthdName.startsWith("is")) {
            if(mthdName.length() <= 2) {
                return null;
            }
            if(!method.getReturnType().equals(Boolean.class) && !method.getReturnType().equals(Boolean.TYPE)) {
                return null;
            }
            StringBuffer buffer = new StringBuffer(StringUtils.removeStart(mthdName, "is"));
            buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
            return buffer.toString();
        }
        return null;
    }
}
