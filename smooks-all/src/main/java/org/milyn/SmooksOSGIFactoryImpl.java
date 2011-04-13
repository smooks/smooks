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

import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksOSGIFactoryImpl implements SmooksOSGIFactory
{
    public Smooks create(final Bundle bundle) throws IOException, SAXException
    {
        return create(bundle, (String) bundle.getHeaders().get("Smooks-Config"));
    }
    
    public Smooks create(final Bundle bundle, final String config) throws IOException, SAXException
    {
        final Smooks smooks = new Smooks();
        smooks.setClassLoader(new BundleClassLoaderDelegator(bundle, getClass().getClassLoader()));
        if (config != null)
        {
	        smooks.addConfigurations(config);
	        smooks.getApplicationContext().setAttribute("ConfiguredWithOSGIHeader", true);	        
        }
        return smooks;
    }
    
}
