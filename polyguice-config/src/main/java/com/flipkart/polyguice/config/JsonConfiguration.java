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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

/**
 * Provides a configuration source for Apache Commons configuration that loads
 * values from a JSON encoded file.
 * <p>
 *
 * @author poroshuram
 */

public class JsonConfiguration extends AbstractConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonConfiguration.class);

    private Map<String, Object> configTab;

    public JsonConfiguration(String path) throws IOException {
        FileReader reader = new FileReader(path);
        load(reader);
        reader.close();
    }

    public JsonConfiguration(File file) throws IOException {
        FileReader reader = new FileReader(file);
        load(reader);
        reader.close();
    }

    public JsonConfiguration(URL url) throws IOException {
        InputStreamReader reader = new InputStreamReader(url.openStream());
        load(reader);
        reader.close();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of base class AbstractConfiguration

    @Override
    protected void addPropertyDirect(String s, Object o) {

    }

    @Override
    public boolean isEmpty() {
        return configTab.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return configTab.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        return configTab.get(key);
    }

    @Override
    public Iterator<String> getKeys() {
        return configTab.keySet().iterator();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private void load(Reader in) {
        configTab = new HashMap<>();
        JsonStreamParser parser = new JsonStreamParser(in);
        JsonElement root = null;
        if(parser.hasNext()) {
            root = parser.next();
        }
        if(root != null && root.isJsonObject()) {
            flatten(null, root);
        }
        LOGGER.debug("json configuration loaded: {}", configTab);
    }

    private void flatten(String prefix, JsonElement element) {
        if(element.isJsonPrimitive()) {
            JsonPrimitive jsonPrim = element.getAsJsonPrimitive();
            if(jsonPrim.isBoolean()) {
                configTab.put(prefix, jsonPrim.getAsBoolean());
            }
            else if(jsonPrim.isNumber()) {
                configTab.put(prefix, jsonPrim.getAsNumber());
            }
            else if(jsonPrim.isString()) {
                configTab.put(prefix, jsonPrim.getAsString());
            }
        }
        else if(element.isJsonObject()) {
            JsonObject jsonObj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
                String prefix1 = ((prefix != null) ? prefix + "." : "") + entry.getKey();
                flatten(prefix1, entry.getValue());
            }
        }
    }
}
