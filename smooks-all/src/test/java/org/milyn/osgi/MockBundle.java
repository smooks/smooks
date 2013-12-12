/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.osgi;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author Daniel Bevenius
 *
 */
public class MockBundle implements Bundle
{
    private JavaArchive jar;

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
