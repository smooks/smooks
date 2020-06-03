/*-
 * ========================LICENSE_START=================================
 * Smooks :: All
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class BundleClassLoaderDelegator extends ClassLoader
{
    private Bundle bundle;
    private ClassLoader classLoader;
    
    public BundleClassLoaderDelegator(final Bundle bundle)
    {
        this.bundle = bundle;
    }

    public BundleClassLoaderDelegator(final Bundle bundle, final ClassLoader delegate)
    {
        super();
        this.bundle = bundle;
        this.classLoader = delegate;
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException
    {
        return bundle.loadClass(name);
    }
    
    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
    {
        Class<?> c = null;
        try
        {
            c = findClass(name);
        }
        catch (final ClassNotFoundException e)
        {
            c = classLoader.loadClass(name);
        }
        
        if (resolve)
        {
            resolveClass(c);
        }
        
        return c;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException
    {
        return bundle.getResources(name);
    }
    
    @Override
    protected URL findResource(String name)
    {
        return bundle.getResource(name);
    }
    
    @Override
    public URL getResource(String name)
    {
        URL resource = findResource(name);
        if (resource == null)
        {
	        resource = classLoader.getResource(name);
        }
        return resource;
    }
    
}
