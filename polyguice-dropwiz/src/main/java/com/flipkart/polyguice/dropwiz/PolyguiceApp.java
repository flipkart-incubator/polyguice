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

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.support.Polyguice;

/**
 * @author indroneel.das
 *
 */

public class PolyguiceApp<T extends Configuration> extends Application<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolyguiceApp.class);

    private List<String>  resPkgNames;
    private Polyguice     polyguice;

    public PolyguiceApp() {
        resPkgNames = new ArrayList<>();
    }

    public final PolyguiceApp<T> setPolyguice(Polyguice pg) {
        polyguice = pg;
        return this;
    }

    public final PolyguiceApp<T> scanPackage(String name) {
        resPkgNames.add(name);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods of base class Application

    @Override
    public final void run(T config, Environment env) throws Exception {

        if(polyguice == null) {
            throw new RuntimeException("polyguice not set");
        }

        if(polyguice.isPrepared()) {
            throw new RuntimeException("polyguice should not be prepared yet");
        }

        DropConfigProvider dcp = new DropConfigProvider(config);
        polyguice.registerConfigurationProvider(dcp);
        polyguice.prepare();

        Set<Class<?>> resTypes = findResourceTypes();
        LOGGER.debug("found potential resources: {}", resTypes);
        for(Class<?> cls : resTypes) {
            try {
                Object resource = createResource(cls, config, env);
                if(resource != null) {
                    LOGGER.debug("resource created: {}", cls.getName());
                    polyguice.getComponentContext().inject(resource);
                    env.jersey().register(resource);
                }
                else {
                    LOGGER.warn("error creating resource: {}", cls.getName());
                }
            }
            catch(Exception exep) {
                LOGGER.error(exep.getMessage(), exep);
            }
        }

        env.lifecycle().manage(new PolyguiceManaged());
        postRun(config, env);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods to be implemented or used from derived classes

    protected final Polyguice getPolyguice() {
        return polyguice;
    }

    protected void postRun(T config, Environment env) throws Exception {
        //NOOP
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private Set<Class<?>> findResourceTypes() {
/*
 * This is a hack around the Reflections package. FilterBuilder does not
 * seem to work with multiple package names, so the list of packages
 * must be provided in a loop
 */

        FilterBuilder fb = new FilterBuilder();
        for(String pkgName : resPkgNames) {
            fb.includePackage(pkgName);
        }
        ConfigurationBuilder cb = new ConfigurationBuilder()
            .filterInputsBy(fb)
            .setUrls(ClasspathHelper.forClassLoader())
            .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        Reflections reflections = new Reflections(cb);

        return reflections.getTypesAnnotatedWith(Resource.class);
    }

    private Object createResource(Class<?> cls, T config, Environment env)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        LOGGER.debug("creating object of type {}", cls);
        try {
            Constructor<?> ctor = cls.getConstructor(Configuration.class, Environment.class);
            int mod = ctor.getModifiers();
            if(Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                LOGGER.debug("using ctor {}", ctor.toGenericString());
                return ctor.newInstance(config, env);
            }
        }
        catch(NoSuchMethodException exep) {
            //NOOP, not even log
        }

        try {
            Constructor<?> ctor = cls.getConstructor(Environment.class, Configuration.class);
            int mod = ctor.getModifiers();
            if(Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                LOGGER.debug("using ctor {}", ctor.toGenericString());
                return ctor.newInstance(env, config);
            }
        }
        catch(NoSuchMethodException exep) {
            //NOOP, not even log
        }

        try {
            Constructor<?> ctor = cls.getConstructor(Configuration.class);
            int mod = ctor.getModifiers();
            if(Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                LOGGER.debug("using ctor {}", ctor.toGenericString());
                return ctor.newInstance(config);
            }
        }
        catch(NoSuchMethodException exep) {
            //NOOP, not even log
        }

        try {
            Constructor<?> ctor = cls.getConstructor(Environment.class);
            int mod = ctor.getModifiers();
            if(Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                LOGGER.debug("using ctor {}", ctor.toGenericString());
                return ctor.newInstance(env);
            }
        }
        catch(NoSuchMethodException exep) {
            //NOOP, not even log
        }

        try {
            Constructor<?> ctor = cls.getConstructor();
            int mod = ctor.getModifiers();
            if(Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                LOGGER.debug("using ctor {}", ctor.toGenericString());
                return ctor.newInstance();
            }
        }
        catch(NoSuchMethodException exep) {
            //NOOP, not even log
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class for managing Polyguice

    private class PolyguiceManaged implements Managed {

        public void start() throws Exception {
            /*
             * NOOP. Polyguice must already be prepared and started before this
             * method is called.
             */
        }

        @Override
        public void stop() throws Exception {
            if(polyguice != null) {
                polyguice.stop();
            }
        }
    }
}
