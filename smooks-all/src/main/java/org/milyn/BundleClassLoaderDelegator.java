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
package org.milyn;

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
