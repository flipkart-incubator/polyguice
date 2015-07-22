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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtilsBean;

import com.flipkart.polyguice.core.ConfigurationProvider;

/**
 * @author indroneel.das
 *
 */

public class DropConfigProvider implements ConfigurationProvider {

    private Configuration     dwConfig;
    private PropertyUtilsBean pub;

    public DropConfigProvider(Configuration config) {
        dwConfig = config;
        pub = new PropertyUtilsBean();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface ConfigurationProvider

    @Override
    public boolean contains(String path) {
        try {
            return (pub.getProperty(dwConfig, path) != null);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exep) {
            //NOOP
        }
        return false;
    }

    @Override
    public Object getValue(String path, Class<?> type) {
        Object value = null;
        try {
            value = pub.getProperty(dwConfig, path);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exep) {
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
}
