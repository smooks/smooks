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
package example;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Element;

/**
 * Basic transformer that simply renames an element.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BasicJavaTransformer implements DOMElementVisitor {

    @ConfigParam(name = "newName", defaultVal = "xxx")
    private String newElementName;

    public void visitBefore(Element element, ExecutionContext executionContext) {
        // Not doing anything on this visit - wait untill after visiting the elements child content...
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
        // Just rename the target element - keeping child elements - not keeping attributes.
        DomUtils.renameElement(element, newElementName, true, false);
    }
}
