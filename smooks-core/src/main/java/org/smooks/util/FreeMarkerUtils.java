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
package org.smooks.util;

import freemarker.ext.dom.NodeModel;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.DOMModel;
import org.smooks.javabean.context.BeanContext;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FreeMarker utility class.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class FreeMarkerUtils {

    /**
     * Get a "merged" model for FreeMarker templating.
     * <p/>
     * This utility merges the current set of beans being managed by the
     * {@link BeanContext} associated with the
     * current {@link ExecutionContext}, with the contents of the {@link DOMModel}
     * associated with the current {@link ExecutionContext}.  This is very useful
     * for templating with FreeMarker.
     *
     * @param executionContext The current execution context.
     * @return A merged templating model.
     */
    public static Map<String, Object> getMergedModel(ExecutionContext executionContext) {
        Map<String, Object> beans = executionContext.getBeanContext().getBeanMap();
        Map<String, Object> model = beans;
        DOMModel domModel = DOMModel.getModel(executionContext);

        if(!domModel.getModels().isEmpty()) {
            Map<String, ElementToNodeModel> elementToNodeModelMap = getElementToNodeModelMap(executionContext);

            model = new HashMap<String, Object>();
            model.putAll(beans);

            Set<Map.Entry<String, Element>> models = domModel.getModels().entrySet();
            for (Map.Entry<String, Element> entry : models) {
                NodeModel nodeModel = getNodeModel(entry.getKey(), entry.getValue(), elementToNodeModelMap);
                model.put(entry.getKey(), nodeModel);
            }
        }

        return model;
    }

    private static NodeModel getNodeModel(String key, Element element, Map<String, ElementToNodeModel> elementToNodeModelMap) {
        ElementToNodeModel elementToNodeModel = elementToNodeModelMap.get(key);

        if(elementToNodeModel == null) {
            elementToNodeModel = new ElementToNodeModel();
            elementToNodeModelMap.put(key, elementToNodeModel);
            elementToNodeModel.element = element;
            elementToNodeModel.nodeModel = NodeModel.wrap(element);
        } else if(elementToNodeModel.element != element) {
            // Must be a new element with the same name... update the map...
            elementToNodeModel.element = element;
            elementToNodeModel.nodeModel = NodeModel.wrap(element);
        }

        return elementToNodeModel.nodeModel;
    }


	private static Map<String, ElementToNodeModel> getElementToNodeModelMap(ExecutionContext executionContext) {
		@SuppressWarnings("unchecked")
		Map<String, ElementToNodeModel> map = (Map<String, ElementToNodeModel>) executionContext.getAttribute(ElementToNodeModel.class);

        if(map == null) {
            map = new HashMap<String, ElementToNodeModel>();
            executionContext.setAttribute(ElementToNodeModel.class, map);
        }

        return map;
    }

    private static class ElementToNodeModel {
        private Element element;
        private NodeModel nodeModel;
    }
}
