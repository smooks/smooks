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
package org.milyn.payload;

import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.payload.JavaResult;
import org.milyn.util.CollectionsUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * An Export instance represents information about the type of data that Smooks can produce/export.
 * </p>
 * 
 * An Export instance must have a 'type' which is the class type of Smooks produces when filtering.
 * </p>
 * An Export can optionally have a name which is simply used as an identifier so that the calling 
 * process can easliy identify this export
 * </p>
 * An Export may also optionally have an 'extract' attribute which can be used when only a sub-part 
 * of the data is to be considered for exporting. 
 * For example, this could be used to specify that  the calling process is only interested in a specific object instance
 * in a {@link JavaResult}
 * </p>
 * 
 * @author Daniel Bevenius
 * @since 1.4
 */
public class Export implements ContentHandler<Export>
{
    @ConfigParam (use = Use.OPTIONAL)
    private String name;
    
    @ConfigParam
    private Class<?> type;
    
    @ConfigParam (use = Use.OPTIONAL)
    private String extract;
    private Set<String> extractSet;

    @AppContext
    private ApplicationContext applicationContext;

    public Export()
    {
    }
    
    public Export(final Class<?> type)
    {
        this.type = type;
    }
    
    public Export(final Class<?> type, final String name)
    {
        this(type);
        this.name = name;
    }
    
    public Export(final Class<?> type, final String name, final String extract)
    {
        this(type, name);
        this.extract = extract;
        initExtractSet();
    }
    
    @Initialize
    public void addToExportsInApplicationContext()
    {
        initExtractSet();
        Exports.addExport(applicationContext, this);
    }

    private void initExtractSet() {
        if(extract != null) {
            extractSet = CollectionsUtil.toSet(extract.split(","));
        }
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getType()
    {
        return type;
    }

    public String getExtract()
    {
        return extract;
    }

    public Set<String> getExtractSet() {
        return extractSet;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extract == null) ? 0 : extract.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        
        if (!(obj instanceof Export))
            return false;
        
        final Export other = (Export) obj;
        return (type == other.type || (type != null && type.equals(other.type))) &&
	        (extract == other.extract || (extract != null && extract.equals(other.extract))) &&
	        (name == other.name || (name != null && name.equals(other.name)));
    }

    public String toString()
    {
        return "Export [type=" + type.getName() + ", name=" + name + ", extract=" + extract + "]";
    }
    
}
