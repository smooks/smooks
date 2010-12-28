/*
	Milyn - Copyright (C) 2006

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
package org.milyn.distro.html.visitors;

import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.dom.Phase;
import org.milyn.delivery.dom.VisitPhase;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Html element ID logger.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Phase(value = VisitPhase.ASSEMBLY)
public class IdLogger implements DOMVisitBefore {

    private static final String CONTEXT_KEY = IdLogger.class.getName() + "#PAGE_IDS";
    
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(element.hasAttribute("id")) {
            getIdMap(executionContext).put(element.getAttribute("id"), null);
        } else if(element.hasAttribute("name")) {
            getIdMap(executionContext).put(element.getAttribute("name"), null);
        }
    }

    public static boolean isInternalId(String id, ExecutionContext executionContext) {
        return getIdMap(executionContext).containsKey(id);
    }

    private static Map getIdMap(ExecutionContext executionContext) {
        Map idMap = (Map) executionContext.getAttribute(CONTEXT_KEY);
        if(idMap == null) {
            idMap = new HashMap();
            executionContext.setAttribute(CONTEXT_KEY, idMap);
        }
        return idMap;
    }
}
