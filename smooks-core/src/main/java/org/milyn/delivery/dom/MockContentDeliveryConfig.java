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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Mock ContentDeliveryConfig for DOM.
 *
 * @author tfennelly
 */
@SuppressWarnings({ "unchecked", "unused" })
public class MockContentDeliveryConfig extends DOMContentDeliveryConfig {
  private final Map objectsHash = new LinkedHashMap();

  public MockContentDeliveryConfig() {
    setSmooksResourceConfigurations(new LinkedHashMap<String, List<SmooksResourceConfiguration>>());
    setAssemblyVisitBefores(new ContentHandlerConfigMapTable());
    setAssemblyVisitAfters(new ContentHandlerConfigMapTable());
    setProcessingVisitBefores(new ContentHandlerConfigMapTable());
    setProcessingVisitAfters(new ContentHandlerConfigMapTable());
    setSerializationVisitors(new ContentHandlerConfigMapTable());
    setVisitCleanables(new ContentHandlerConfigMapTable());
  }

  /* (non-Javadoc)
   * @see org.milyn.delivery.ContentDeliveryConfig#getObjects(java.lang.String)
   */
  public List getObjects(String selector) {
    return (List) objectsHash.get(selector);
  }

  public void addObject(String selector, Object object) {
    List objects = (List) objectsHash.get(selector);

    if (objects == null) {
      objects = new Vector();
      objectsHash.put(selector, objects);
    }
    objects.add(object);
  }
}
