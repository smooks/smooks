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
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ElementOperator implements DOMVisitBefore {

    @ConfigParam
    private String attribName;

    @ConfigParam
    private String attribValue;

    @ConfigParam
    private String operation;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(element.getAttribute(attribName).contains(attribValue)) {
            if(operation.equals("removeText")) {
                removeText(element);
            } else if(operation.equals("remove")) {
                DomUtils.removeElement(element, false);
            }
        }
    }

    private void removeText(Element element) {
        List children = DomUtils.copyNodeList(element.getChildNodes());

        for (Object child : children) {
            if(child instanceof Text) {
                element.removeChild((Node) child);
            }
        }
    }
}
