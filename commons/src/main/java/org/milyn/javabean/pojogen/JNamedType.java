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
package org.milyn.javabean.pojogen;

import org.milyn.assertion.AssertArgument;

/**
 * Named type for properties and method parameters.
 * @author bardl
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JNamedType {
    
    private JType type;
    private String name;

    public JNamedType(JType type, String name) {
        AssertArgument.isNotNull(type, "type");
        AssertArgument.isNotNull(name, "name");
        this.type = type;
        this.name = name;
    }

    public JType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JNamedType) {
            JNamedType namedTypeObj = (JNamedType) obj;

            if(namedTypeObj.getName().equals(name) && namedTypeObj.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (name.hashCode() + type.hashCode());
    }
}