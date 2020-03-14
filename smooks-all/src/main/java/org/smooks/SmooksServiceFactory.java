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
package org.smooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * SmooksServiceFactory is a {@link ServiceFactory} implementation that
 * enables the creation of per-bundle {@link SmooksOSGIFactory} implementations.
 * </p>
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksServiceFactory implements ServiceFactory
{
    /**
     * Will return a {@link SmooksOSGIFactory} instance configured with the {@link Bundle}. 
     * 
     * @param bundle the OSGi bundle.
     * @param registration the OSGi registration.
     * @return {@link SmooksOSGIFactory} that has been configured using the passed-in Bundle.
     * 
     */
    public Object getService(final Bundle bundle, final ServiceRegistration registration)
    {
        return new SmooksOSGIFactory(bundle);
    }

    public void ungetService(final Bundle bundle, final ServiceRegistration registration, final Object service)
    {
        //Noop
    }

}
