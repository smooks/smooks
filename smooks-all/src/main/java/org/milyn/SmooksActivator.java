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

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SmooksActivator implements BundleActivator
{
    private ServiceRegistration registerService;

    public void start(BundleContext context) throws Exception
    {
        System.out.println("Starting Smooks Bundle [" + context.getBundle().getHeaders().get("Bundle-Version") + "]");
        SmooksServiceFactory smooksOSGIFactory = new SmooksServiceFactory();
        registerService = context.registerService(Smooks.class.getName(), smooksOSGIFactory, new Properties());
        System.out.println("Registered : "  + Smooks.class.getName());
        
    }

    public void stop(BundleContext context) throws Exception
    {
        System.out.println("Stopping Smooks Bundle [" + context.getBundle().getHeaders().get("Bundle-Version") + "]");
        registerService.unregister();
    }

}
