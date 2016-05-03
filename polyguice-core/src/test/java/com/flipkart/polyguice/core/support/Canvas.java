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

import com.flipkart.polyguice.core.Component;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author indroneel.das
 */

@Component("canvas")
public class Canvas {

    @Inject
    @Named("triangle")
    private Shape triangle;

    @Inject
    @Named("square")
    private Shape square;

    @Inject
    @Named("circle")
    private Shape circle;


    public Shape getTriangle() {
        return triangle;
    }

    public Shape getSquare() {
        return square;
    }

    public Shape getCircle() {
        return circle;
    }
}
