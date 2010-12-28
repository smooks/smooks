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

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class ResourceFilter implements Filter<ArchivePath>
{
    private final String resourceName;

    public ResourceFilter(final String resourceName)
    {
        this.resourceName = resourceName;
    }

    public boolean include(ArchivePath object)
    {
        if (object.get().toString().equals(resourceName))
            return true;
        else
            return false;
    }

}
