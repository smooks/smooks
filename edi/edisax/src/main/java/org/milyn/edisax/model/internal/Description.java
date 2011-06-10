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

package org.milyn.edisax.model.internal;

import org.milyn.assertion.AssertArgument;

public class Description {

    private String name;
    private String version;
    private String namespace;

    public String getName() {
        return name;
    }

    public Description setName(String name) {
    	AssertArgument.isNotNull(name, "name");
        this.name = name.trim();
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Description setVersion(String version) {
    	AssertArgument.isNotNull(version, "version");
        this.version = version.trim();
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public Description setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public Object clone() {
        return new Description().setName(name).setVersion(version).setNamespace(namespace);
    }

    @Override
	public boolean equals(Object obj) {
		assertInitialized();		

		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		
		if(obj instanceof Description) {
			Description description = (Description) obj;
			return description.name.equals(name) && description.version.equals(version);
		} else if (obj instanceof String) {
			// Just comparing the names and ignoring the version...
			return obj.equals(name);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		assertInitialized();		
		return (name + "#" + version).hashCode();
	}

	private void assertInitialized() {
		if(name == null || version == null) {
			throw new IllegalStateException("Description 'name' and/or 'version' properties are not initialized.");
		}
	}
}
