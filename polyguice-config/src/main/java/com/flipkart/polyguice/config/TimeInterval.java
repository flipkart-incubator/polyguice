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

import org.apache.commons.lang3.StringUtils;

/**
 * This class encapsulates a time interval value as a delta between two events.
 * Internally, the value is always stored in milliseconds.
 * <p>
 *
 * At the time of creation, the time interval is always specified as a string
 * input. The input contains a numeric part suffixed with a 's', 'm', 'h' or 'd'
 * to signify that the numeric value is in seconds, minutes, hours or days. Note
 * that there should not be any whitespace between the numeric part and the
 * suffix.
 * <p>
 *
 * @author indroneel.das
 */

public class TimeInterval {

    private long interval;

/**
 * Creates a time interval with internal value of -1 (invalid/unset value).
 * <p>
 */

    public TimeInterval() {
        interval = -1;
    }

/**
 * Creates a time interval populated with information from the specified string
 * value.
 * <p>
 *
 * @param	timeValue the information for initializing a time inerval value.
 */

    public TimeInterval(String timeValue) {
        interval = -1;
        parseInterval(timeValue);
    }

/**
 * Populates the time interval with information from the specified string value.
 * <p>
 *
 * @param	timeValue the information for initializing a time inerval value.
 */

    public void setValue(String timeValue) {
        parseInterval(timeValue);
    }

/**
 * Retrieves the internally stored time interval value.
 * <p>
 *
 * @return	the time interval in milliseconds.
 */

    public long getValue() {
        return interval;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private void parseInterval(String timeValue) {

        if(StringUtils.isBlank(timeValue)) {
            return;
        }
        timeValue = timeValue.toLowerCase();
        long factor = 1;
        if(timeValue.endsWith("s")) {
            factor = 1000;
        }
        if(timeValue.endsWith("m")) {
            factor = 60*1000;
        }
        if(timeValue.endsWith("h")) {
            factor = 60*60*1000;
        }
        if(timeValue.endsWith("d")) {
            factor = 24*60*60*1000;
        }

        if(timeValue.endsWith("s") || timeValue.endsWith("m")
                || timeValue.endsWith("h") || timeValue.endsWith("d")) {
            timeValue = timeValue.substring(0, timeValue.length() - 1);
        }

        try {
            double value = Double.parseDouble(timeValue);
            interval = (long) (value * factor);
        }
        catch(NumberFormatException exep)
        {	interval = -1;
        }
    }
}
