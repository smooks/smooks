/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.payload;

import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.ConfigParam.Use;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.util.CollectionsUtil;

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
