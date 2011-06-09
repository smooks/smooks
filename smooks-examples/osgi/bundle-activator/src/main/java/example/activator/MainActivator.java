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

package example.activator;

import org.milyn.Smooks;
import org.milyn.SmooksFactory;
import org.milyn.SmooksOSGIFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import example.ExampleUtil;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class MainActivator implements BundleActivator
{
    private Smooks smooks;
    
    public void start(BundleContext context) throws Exception
    {
        try
        {
	        final SmooksFactory smooksOSGIFactory = new SmooksOSGIFactoryImpl(context.getBundle());
	        final String config = (String) context.getBundle().getHeaders().get("Smooks-Config");
	        smooks = smooksOSGIFactory.create(config);
	        performFiltering(context);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void performFiltering(BundleContext context)
    {
        String input = (String) context.getBundle().getHeaders().get("Smooks-Input-File");
        ExampleUtil.performFiltering(input, smooks);
    }
	
    public void stop(BundleContext context) throws Exception
    {
        System.out.println("MainActivator stop");
        if (smooks != null)
        {
	        smooks.close();
        }
    }
    
}
