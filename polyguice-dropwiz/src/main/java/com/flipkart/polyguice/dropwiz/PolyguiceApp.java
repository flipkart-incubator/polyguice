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

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.support.Polyguice;

/**
 * @author indroneel.das
 *
 */

public class PolyguiceApp<T extends Configuration> extends Application<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolyguiceApp.class);

    private List<String>  scanPkgNames;
    private Polyguice     polyguice;

    public PolyguiceApp() {
        scanPkgNames = new ArrayList<>();
    }

    public final PolyguiceApp<T> setPolyguice(Polyguice pg) {
        polyguice = pg;
        return this;
    }

    public final PolyguiceApp<T> scanPackage(String name) {
        scanPkgNames.add(name);
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

        preRun(config, env);

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

        List<Class<?>> servletTypes = findServletTypes();
        LOGGER.debug("found potential servlets: {}", servletTypes);
        for(Class<?> type : servletTypes) {
            registerServlet(type, env);
        }

        env.lifecycle().manage(new PolyguiceManaged());
        postRun(config, env);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods to be implemented or used from derived classes

    protected final Polyguice getPolyguice() {
        return polyguice;
    }

    protected void preRun(T config, Environment env) throws Exception {
        //NOOP
    }

    protected void postRun(T config, Environment env) throws Exception {
        //NOOP
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private Set<Class<?>> findResourceTypes() {
        Reflections reflections = new Reflections(scanPkgNames.toArray());
        return reflections.getTypesAnnotatedWith(Resource.class);
    }

    private List<Class<?>> findServletTypes() {
        Reflections reflections = new Reflections(scanPkgNames.toArray());
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(WebServlet.class);
        List<Class<?>> result = new ArrayList<>();
        for(Class<?> type : types) {
            if(Servlet.class.isAssignableFrom(type)) {
                result.add(type);
            }
        }
        return result;
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

    private void registerServlet(Class<?> type, Environment env) {
        LOGGER.debug("registering servlet: {}", type.getName());
        WebServlet ann = type.getAnnotation(WebServlet.class);
        String srvName = ann.name();
        if(StringUtils.isBlank(srvName)) {
            LOGGER.error("servlet {}: name could not be blank", type.getName());
            return;
        }
        String[] paths = ann.urlPatterns();
        if(paths == null || paths.length == 0) {
            paths = ann.value();
            if(paths == null || paths.length == 0) {
                LOGGER.error("url patterns missing for servlet {}", type.getName());
                return;
            }
        }
        int losu = ann.loadOnStartup();
        Servlet servlet = null;
        try {
            servlet = (Servlet) type.newInstance();
            polyguice.getComponentContext().inject(servlet);
        }
        catch(Exception exep) {
            LOGGER.error("error creating servlet {}", type.getName());
            return;
        }
        ServletRegistration.Dynamic dynamic = env.servlets().addServlet(srvName, servlet);
        dynamic.addMapping(paths);
        dynamic.setLoadOnStartup(losu);
        if(ann.initParams() == null) {
            return;
        }
        for(WebInitParam param : ann.initParams()) {
            String name = param.name();
            String value = param.value();
            if(StringUtils.isNoneBlank(name)) {
                dynamic.setInitParameter(name, value);
            }
        }
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
