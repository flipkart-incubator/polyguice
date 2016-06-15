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

package com.flipkart.polyguice.config;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.ConfigurationProvider;

/**
 * @author indroneel.das
 *
 */

public class ApacheCommonsConfigProvider implements ConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheCommonsConfigProvider.class);

    private CompositeConfiguration rootConfig;

    public ApacheCommonsConfigProvider() {
        rootConfig = new CompositeConfiguration();
    }

    public ApacheCommonsConfigProvider location(String filePath) {
        Class<? extends Configuration> clazz = selectConfigImpl(filePath);
        try {
            if (clazz != null) {
                this.rootConfig.addConfiguration(clazz.getConstructor(File.class).newInstance(new File(filePath)));
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    public ApacheCommonsConfigProvider location(URL url) {
        Class<? extends Configuration> clazz = selectConfigImpl(url.getPath());
        try {
            if (clazz != null) {
                this.rootConfig.addConfiguration(clazz.getConstructor(URL.class).newInstance(url));
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of interface ConfigurationProvider

    @Override
    public boolean contains(String path) {
        return rootConfig.containsKey(path);
    }

    @Override
    public Object getValue(String path, Class<?> type) {
        if(!rootConfig.containsKey(path)) {
            return null;
        }
        if(type.equals(Byte.TYPE) || type.equals(Byte.class)) {
            return rootConfig.getByte(path, null);
        }
        else if(type.equals(Short.TYPE) || type.equals(Short.class)) {
            return rootConfig.getShort(path, null);
        }
        else if(type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return rootConfig.getInteger(path, null);
        }
        else if(type.equals(Long.TYPE) || type.equals(Long.class)) {
            return rootConfig.getLong(path, null);
        }
        else if(type.equals(Float.TYPE) || type.equals(Float.class)) {
            return rootConfig.getFloat(path, null);
        }
        else if(type.equals(Double.TYPE) || type.equals(Double.class)) {
            return rootConfig.getDouble(path, null);
        }
        else if(type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return rootConfig.getBoolean(path, null);
        }
        else if(type.equals(String.class)) {
            return rootConfig.getString(path);
        }
        else if(type.equals(BigInteger.class)) {
            return rootConfig.getBigInteger(path);
        }
        else if(type.equals(BigDecimal.class)) {
            return rootConfig.getBigDecimal(path);
        }
        else if(type.equals(Properties.class)) {
            return rootConfig.getProperties(path);
        }
        else if(type.equals(String[].class)) {
            return rootConfig.getStringArray(path);
        }
        else if(type.equals(TimeInterval.class)) {
            String interval = rootConfig.getString(path);
            if(interval == null) {
                return null;
            }
            return new TimeInterval(interval);
        }
        return null;
    }
    // PRIVATE METHODS

    private Class<? extends Configuration> selectConfigImpl(String loc) {
        try {
            if(loc.toLowerCase(Locale.getDefault()).endsWith(".properties")) {
                LOGGER.debug("properties configuration from {}", loc);
                return PropertiesConfiguration.class;
            }
            else if(loc.toLowerCase(Locale.getDefault()).endsWith(".xml")) {
                LOGGER.debug("xml configuration from {}", loc);
                return XMLConfiguration.class;
            }
            else if(loc.toLowerCase(Locale.getDefault()).endsWith(".json")) {
                LOGGER.debug("json configuration from {}", loc);
                return JsonConfiguration.class;
            }
            else if(loc.toLowerCase(Locale.getDefault()).endsWith(".yml")) {
                LOGGER.debug("yaml configuration from {}", loc);
                return YamlConfiguration.class;
            }
            else if(loc.toLowerCase(Locale.getDefault()).endsWith(".yaml")) {
                LOGGER.debug("yaml configuration from {}", loc);
                return YamlConfiguration.class;
            }
        }
        catch(Exception exep) {
            LOGGER.error("unable to load configuration from " + loc.toString(), exep);
        }
        return null;  // TODO - should ideally throw an exception, but going with the semantics of failing queitly for now
    }

}
