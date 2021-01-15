/*-
 * ========================LICENSE_START=================================
 * Smooks :: All
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
package org.smooks;

import org.osgi.framework.Bundle;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * A factory class for creating Smooks instances in an OSGi environment.
 * </p>
 * This factory will create create a class loader that is able to delegate
 * to the bundles classloader. 
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksOSGIFactory implements SmooksFactory
{
    private final Bundle bundle;

    public SmooksOSGIFactory(final Bundle bundle)
    {
        this.bundle = bundle;
    }

    /**
     * Creates a new Smooks instance using the {@link Bundle} passed in.
     * Calling this method is equivalent to calling {@link #create(String)} and passing 
     * in {@code null} as the config.
     * 
     * @return Smooks a newly created Smooks instance that is un-configured.
     */
    public Smooks createInstance() 
    {
        return createSmooksWithDelegatingClassloader();
    }
    
    /**
     * Creates a new Smooks instance using the {@link Bundle} passed in and adds the passed-in
     * configuration.
     * 
     * @param bundle the OSGi bundle for which a delegating classloader will be created.
     * @param config the configuration that should be added to the newly created Smooks instance. If null
     * then a non-configured Smooks instance will be returned.
     * @return Smooks a newly created Smooks instance.
     */
    public Smooks createInstance(final InputStream config) throws IOException, SAXException {
        final Smooks smooks = createSmooksWithDelegatingClassloader();
        if (config != null)
        {
	        smooks.addConfigurations(config);
        }
        return smooks;
    }
    
    private Smooks createSmooksWithDelegatingClassloader() {
        final Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().setClassLoader(new BundleClassLoaderDelegator(bundle, getClass().getClassLoader())).build());
        return smooks;
    }

    public Smooks createInstance(String config) throws IOException, SAXException
    {
        final Smooks smooks = createSmooksWithDelegatingClassloader();
        if (config != null)
        {
	        smooks.addConfigurations(config);
        }
        return smooks;
    }
    
}
