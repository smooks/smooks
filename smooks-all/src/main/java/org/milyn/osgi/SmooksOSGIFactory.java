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

import org.milyn.Smooks;
import org.milyn.SmooksFactory;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

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
        final Smooks smooks = new Smooks();
        smooks.setClassLoader(new BundleClassLoaderDelegator(bundle, getClass().getClassLoader()));
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
