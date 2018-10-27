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

package org.milyn.delivery.dom;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.ContentHandlerConfigMapTable;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.dom.serialize.SerializationUnit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Mock ContentDeliveryConfig for DOM.
 * @author tfennelly
 */
@SuppressWarnings("unchecked")
public class MockContentDeliveryConfig extends DOMContentDeliveryConfig {
	public  ContentHandlerConfigMapTable serializationUnits = new ContentHandlerConfigMapTable();
	private Map                          objectsHash        = new LinkedHashMap();

    public MockContentDeliveryConfig() {
        setSmooksResourceConfigurations(new LinkedHashMap<String, List<SmooksResourceConfiguration>>());
        setAssemblyVisitBefores(new ContentHandlerConfigMapTable<DOMVisitBefore>());
        setAssemblyVisitAfters(new ContentHandlerConfigMapTable<DOMVisitAfter>());
        setProcessingVisitBefores(new ContentHandlerConfigMapTable<DOMVisitBefore>());
        setProcessingVisitAfters(new ContentHandlerConfigMapTable<DOMVisitAfter>());
        setSerailizationVisitors(new ContentHandlerConfigMapTable<SerializationUnit>());
        setVisitCleanables(new ContentHandlerConfigMapTable<VisitLifecycleCleanable>());
    }

	/* (non-Javadoc)
	 * @see org.milyn.delivery.ContentDeliveryConfig#getObjects(java.lang.String)
	 */
	public List getObjects(String selector) {
		return (List)objectsHash.get(selector);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public void addObject(String selector, Object object) {
		List objects = (List)objectsHash.get(selector);

		if(objects == null) {
			objects = new Vector();
			objectsHash.put(selector, objects);
		}
		objects.add(object);
	}
}
