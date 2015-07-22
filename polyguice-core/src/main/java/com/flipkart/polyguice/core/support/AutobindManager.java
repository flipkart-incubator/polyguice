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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.polyguice.core.Bindable;
import com.flipkart.polyguice.core.Component;
import com.flipkart.polyguice.core.ComponentProcessor;
import com.flipkart.polyguice.core.NonBindable;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 *
 * @author indroneel.das
 *
 */

class AutobindManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutobindManager.class);

    private Binder             binder;
    private List<String>       procNames;
    private List<SingletonKey> singletonKeys;

    AutobindManager(Binder binder) {
        this.binder = binder;
    }

    public void autobind(String[] scanPkgNames) {
        LOGGER.debug("start_autobind");
        for(String pkgName : scanPkgNames) {
            LOGGER.debug("scan_package {}", pkgName);
        }

/*
 * This is a hack around the Reflections package. FilterBuilder does not
 * seem to work with multiple package names, so the list of packages
 * must be provided in a loop
 */

/* For version 1.0 onwards: with reflections upgraded to 0.9.10, this hack seems
 * no longer necessary. Commenting out the same.
 */

/*
        FilterBuilder fb = new FilterBuilder();
        for(String pkgName : scanPkgNames) {
            fb.includePackage(pkgName);
        }

        ConfigurationBuilder cb = new ConfigurationBuilder()
            .filterInputsBy(fb)
            .setUrls(ClasspathHelper.forClassLoader())
            .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
 */
        Reflections reflections = new Reflections((Object[]) scanPkgNames);

        procNames = new ArrayList<>();
        Set<Class<? extends ComponentProcessor>> procClsList = reflections.getSubTypesOf(ComponentProcessor.class);
        LOGGER.debug("component processors: {}", procClsList.size());
        for(Class<?> cls : procClsList) {
            String procId = bindComponentProcessor(cls);
            procNames.add(procId);
        }

        singletonKeys = new ArrayList<>();
        Set<Class<?>> clsList = reflections.getTypesAnnotatedWith(Component.class);
        LOGGER.debug("components: {}", clsList.size());
        for(Class<?> cls : clsList) {
            SingletonKey sk = bindComponent(cls);
            if(sk != null) {
                singletonKeys.add(sk);
            }
        }
        LOGGER.debug("end_autobind");
    }

    public List<String> getComponentProcessors() {
        return procNames;
    }

    public List<SingletonKey> getSingletons() {
        return singletonKeys;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods

    private String bindComponentProcessor(Class<?> cmpCls) {
        String objId = cmpCls.getName() + "#" + Long.toString(System.currentTimeMillis(), 36);
        Named named = Names.named(objId);
        createBindings(cmpCls, null, named);
        LOGGER.debug("bound component processor: {} to {}", cmpCls.getName(), objId);
        return objId;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SingletonKey bindComponent(Class cmpCls) {
        Component ann = (Component) cmpCls.getAnnotation(Component.class);
        LOGGER.debug("binding component: type={}, name={}, namedOnly={}, value={}",
                cmpCls.getName(), ann.name(), ann.namedOnly(), ann.value());

        Named named = null;
        if(ann.value() != null && ann.value().trim().length() > 0) {
            named = Names.named(ann.value());
        }
        else if(ann.name() != null && ann.name().trim().length() > 0) {
            named = Names.named(ann.name());
        }

        Set<Class<?>> ifaces = new HashSet<>();
        retrieveInterfaces(cmpCls, ifaces);
        Set<Class<?>> bindables = retrieveBindables(ifaces);
        if(ann.namedOnly()) {
            return createBindings(cmpCls, null, named);
        }
        else {
            if(bindables.isEmpty()) {
                return createBindings(cmpCls, ifaces, named);
            }
            else {
                return createBindings(cmpCls, bindables, named);
            }
        }
    }

    private void retrieveInterfaces(Class<?> cls, Set<Class<?>> interfaces) {
        Class<?>[] ifaces = cls.getInterfaces();
        if(ifaces.length == 0) {
            return;
        }
        for(Class<?> iface : ifaces) {
            if(iface.getAnnotation(NonBindable.class) == null) {
                interfaces.add(iface);
            }
        }
        for(Class<?> iface : ifaces) {
            retrieveInterfaces(iface, interfaces);
        }
    }

    private Set<Class<?>> retrieveBindables(Set<Class<?>> interfaces) {
        Set<Class<?>> result = new HashSet<Class<?>>();
        for(Class<?> iface : interfaces) {
            if(iface.getAnnotation(Bindable.class) != null) {
                result.add(iface);
            }
        }
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SingletonKey createBindings(Class cmpCls, Set<Class<?>> ifaces, Named named) {
        boolean hasSingletonAnn = (cmpCls.getAnnotation(Singleton.class) != null);
        SingletonKey sk = null;
        if(ifaces != null && !ifaces.isEmpty()) {
            for(Class<?> iface : ifaces) {
                if(named != null) {
                    binder.bind(iface).annotatedWith(named).to(cmpCls);
                    LOGGER.debug("bound iface: {}, named: {}, to: {}", iface.getName(), named.value(), cmpCls.getName());
                    if(hasSingletonAnn) {
                        sk = new SingletonKey(iface, named);
                    }
                }
                else {
                    binder.bind(iface).to(cmpCls);
                    LOGGER.debug("bound iface: {}, to: {}", iface.getName(), cmpCls.getName());
                    if(hasSingletonAnn) {
                        sk = new SingletonKey(iface, null);
                    }
                }
            }
        }
        else if(named != null) {
            binder.bind(Object.class).annotatedWith(named).to(cmpCls);
            LOGGER.debug("bound named: {}, to: {}", named.value(), cmpCls.getName());
            if(hasSingletonAnn) {
                sk = new SingletonKey(null, named);
            }
        }
        else {
            Named dyname = Names.named(cmpCls.getClass().getName() + "#" + Long.toString(System.currentTimeMillis(), 36));
            binder.bind(Object.class).annotatedWith(dyname).to(cmpCls);
            LOGGER.debug("bound named: {}, to: {}", dyname.value(), cmpCls.getName());
            if(hasSingletonAnn) {
                sk = new SingletonKey(null, named);
            }
        }
        return sk;
    }
}
