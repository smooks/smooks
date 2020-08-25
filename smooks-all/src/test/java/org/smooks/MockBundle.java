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


import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class MockBundle implements Bundle
{
    private final JavaArchive jar;

    public MockBundle(final JavaArchive jar)
    {
        this.jar = jar;
    }
    
    public int getState()
    {
        return 0;
    }
    public void start() throws BundleException { }
    public void stop() throws BundleException { }
    public void update() throws BundleException { }
    public void update(InputStream in) throws BundleException { }
    public void uninstall() throws BundleException { }

    public Dictionary getHeaders()
    {
        return null;
    }

    public long getBundleId()
    {
        return 0;
    }

    public String getLocation()
    {
        return null;
    }

    public ServiceReference[] getRegisteredServices()
    {
        return null;
    }

    public ServiceReference[] getServicesInUse()
    {
        return null;
    }

    public boolean hasPermission(Object permission)
    {
        return false;
    }

    public URL getResource(String name)
    {
        Node node = jar.get(name);
        if (node == null)
        {
            return null;
        }
        
        try
        {
            return new URL("file://" + name);
        } 
        catch (final MalformedURLException e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Dictionary getHeaders(String locale)
    {
        return null;
    }

    public String getSymbolicName()
    {
        return null;
    }

    public Class loadClass(String name) throws ClassNotFoundException
    {
        return null;
    }

    public Enumeration getResources(String name) throws IOException
    {
        return null;
    }

    public Enumeration getEntryPaths(String path)
    {
        return null;
    }

    public URL getEntry(String name)
    {
        return null;
    }

    public long getLastModified()
    {
        return 0;
    }

    public Enumeration findEntries(String path, String filePattern, boolean recurse)
    {
        return null;
    }

}
