/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.injector.Scope;
import org.smooks.cdr.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.cdr.registry.lookup.LifecycleManagerLookup;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.container.ApplicationContext;
import org.smooks.util.ClassUtil;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Java ContentHandler instance creator.
 * <p/>
 * Java-based ContentHandler implementations should contain a public
 * constructor that takes a SmooksResourceConfiguration instance as a parameter.
 * @author tfennelly
 */
public class JavaContentHandlerFactory implements ContentHandlerFactory {

    @Inject
    private ApplicationContext appContext;
    
    private Map<SmooksResourceConfiguration, Object> javaContentHandlers = new ConcurrentHashMap<>();

    /**
     * Create a Java based ContentHandler instance.
     *
     * @param smooksResourceConfiguration The SmooksResourceConfiguration for the Java {@link ContentHandler}
     *                                    to be created.
     * @return Java {@link ContentHandler} instance.
     */
    @SuppressWarnings("unchecked")
    public Object create(final SmooksResourceConfiguration smooksResourceConfiguration) throws SmooksConfigurationException {
        return javaContentHandlers.computeIfAbsent(smooksResourceConfiguration, new Function<SmooksResourceConfiguration, Object>() {
            @Override
            public Object apply(final SmooksResourceConfiguration smooksResourceConfiguration) {
                Object contentHandler;
                try {
                    final String className = ClasspathUtils.toClassName(smooksResourceConfiguration.getResource());
                    final Class<?> classRuntime = ClassUtil.forName(className, getClass());
                    final Constructor<?> constructor;
                    try {
                        constructor = classRuntime.getConstructor(SmooksResourceConfiguration.class);
                        contentHandler = constructor.newInstance(smooksResourceConfiguration);
                    } catch (NoSuchMethodException e) {
                        contentHandler = classRuntime.newInstance();
                    }
                    appContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(contentHandler, new PostConstructLifecyclePhase(new Scope(appContext.getRegistry(), smooksResourceConfiguration, contentHandler)));
                    appContext.getRegistry().registerObject(contentHandler);

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to create an instance of Java ContentHandler [" + smooksResourceConfiguration.getResource() + "].  See exception cause...", e);
                }

                return contentHandler;
            }
        });
    }

    @Override
    public String getType() {
        return "class";
    }
}
