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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.ComponentContext;
import com.google.inject.name.Named;

/**
 *
 * @author indroneel.das
 *
 */

class SingletonKey {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonKey.class);

    private Class<?> type;
    private Named    named;

    SingletonKey(Class<?> type, Named named) {
        this.type = type;
        this.named = named;
    }

    public Object loadComponent(ComponentContext compCtxt) {
        if(type != null && named != null) {
            LOGGER.debug("preloading singleton named: {}, type: {}", named.value(), type.getName());
            return compCtxt.getInstance(named.value(), type);
        }
        else if(type != null) {
            LOGGER.debug("preloading singleton type: {}", type.getName());
            return compCtxt.getInstance(type);
        }
        else if(named != null) {
            LOGGER.debug("preloading singleton named: {}", named.value());
            return compCtxt.getInstance(named.value());
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
