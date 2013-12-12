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
package org.milyn.cartridge.javabean.dynamic;

import org.milyn.cartridge.javabean.dynamic.serialize.DefaultNamespace;

import java.util.List;

@DefaultNamespace(uri = "http://www.acme.com/xsd/bbb.xsd", prefix = "bbb")
public class BBB {

	private List<AAA> aaas;
	private float floatProperty;

    public List<AAA> getAaas() {
        return aaas;
    }

    public void setAaas(List<AAA> aaas) {
        this.aaas = aaas;
    }

    public float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }
}
