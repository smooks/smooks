/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery;

import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerFactory;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.api.SmooksConfigException;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.api.ApplicationContext;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.support.ClassUtil;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Java ContentHandler instance creator.
 * <p/>
 * Java-based ContentHandler implementations should contain a public
 * constructor that takes a ResourceConfig instance as a parameter.
 * @author tfennelly
 */
public class JavaContentHandlerFactory implements ContentHandlerFactory<Object> {

    @Inject
    private ApplicationContext applicationContext;
    
    /**
     * Create a Java based ContentHandler instance.
     *
     * @param resourceConfig The ResourceConfig for the Java {@link ContentHandler}
     *                                    to be created.
     * @return Java {@link ContentHandler} instance.
     */
    @Override
    public Object create(final ResourceConfig resourceConfig) throws SmooksConfigException {
        Object contentHandler;
        try {
            final String className = ClasspathUtils.toClassName(resourceConfig.getResource());
            final Class<?> classRuntime = ClassUtil.forName(className, getClass());
            final Constructor<?> constructor;
            try {
                constructor = classRuntime.getConstructor(DefaultResourceConfig.class);
                contentHandler = constructor.newInstance(resourceConfig);
            } catch (NoSuchMethodException e) {
                contentHandler = classRuntime.newInstance();
            }
            applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(contentHandler, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), resourceConfig, contentHandler)));
            applicationContext.getRegistry().registerObject(contentHandler);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new SmooksException("Failed to create an instance of Java ContentHandler [" + resourceConfig.getResource() + "].  See exception cause...", e);
        }

        return contentHandler;
    }

    @Override
    public String getType() {
        return "class";
    }
}