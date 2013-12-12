/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.commons.classpath;

import org.milyn.commons.assertion.AssertArgument;

/**
 * Filter classpath classes based on their type.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InstanceOfFilter extends AbstractFilter {

    private Class searchType;
    
    public InstanceOfFilter(Class searchType) {
        AssertArgument.isNotNull(searchType, "searchType");
        this.searchType = searchType;
    }

    public InstanceOfFilter(Class searchType, String[] igrnoreList, String[] includeList) {
        super(igrnoreList, includeList);

        AssertArgument.isNotNull(searchType, "searchType");
        this.searchType = searchType;
    }

    protected boolean addClass(Class clazz) {
        return searchType.isAssignableFrom(clazz);
    }
}
