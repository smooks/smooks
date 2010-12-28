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
package org.milyn.delivery;

import org.milyn.container.ExecutionContext;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Node model.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DOMModel {

    private Map<String, Element> models = new LinkedHashMap<String, Element>();

    public Map<String, Element> getModels() {
        return models;
    }

    public static DOMModel getModel(ExecutionContext executionContext) {
        DOMModel nodeModel = (DOMModel) executionContext.getAttribute(DOMModel.class);

        if(nodeModel == null) {
            nodeModel = new DOMModel();
            executionContext.setAttribute(DOMModel.class, nodeModel);
        }

        return nodeModel;
    }
}
