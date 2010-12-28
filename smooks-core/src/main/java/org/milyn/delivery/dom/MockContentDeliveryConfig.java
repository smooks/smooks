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

import java.util.*;

/**
 * Mock ContentDeliveryConfig for DOM. 
 * @author tfennelly
 */
public class MockContentDeliveryConfig extends DOMContentDeliveryConfig {

    private Map<String, List<SmooksResourceConfiguration>> resourceConfigTable = new LinkedHashMap<String, List<SmooksResourceConfiguration>>();
	public ContentHandlerConfigMapTable assemblyBefores = new ContentHandlerConfigMapTable();
    public ContentHandlerConfigMapTable assemblyAfters = new ContentHandlerConfigMapTable();
    public ContentHandlerConfigMapTable processingBefores = new ContentHandlerConfigMapTable();
    public ContentHandlerConfigMapTable processingAfters = new ContentHandlerConfigMapTable();
    public ContentHandlerConfigMapTable serializationUnits = new ContentHandlerConfigMapTable();
    public ContentHandlerConfigMapTable visitCleanables = new ContentHandlerConfigMapTable();
	public Map objectsHash = new LinkedHashMap();

    public MockContentDeliveryConfig() {
        setSmooksResourceConfigurations(resourceConfigTable);
        setAssemblyVisitBefores(assemblyBefores);
        setAssemblyVisitAfters(assemblyAfters);
        setProcessingVisitBefores(processingBefores);
        setProcessingVisitAfters(processingAfters);
        setSerailizationVisitors(serializationUnits);
        setVisitCleanables(visitCleanables);
    }

	/* (non-Javadoc)
	 * @see org.milyn.delivery.ContentDeliveryConfig#getObjects(java.lang.String)
	 */
	public List getObjects(String selector) {
		return (List)objectsHash.get(selector);
	}

	public void addObject(String selector, Object object) {
		List objects = (List)objectsHash.get(selector);
		
		if(objects == null) {
			objects = new Vector();
			objectsHash.put(selector, objects);
		}
		objects.add(object);
	}
}
