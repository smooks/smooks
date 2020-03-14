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
package org.smooks.javabean.ext;

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

/**
 * Bean wiring target attribute checker.
 * <p/>
 * Makes sure that one of "beanIdRef", "beanClass" and "beanAnnotation" are configured.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class WireTargetChecker implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext execContext) throws SmooksException {
        boolean isBeanIdRefSpecified = element.hasAttribute("beanIdRef");
        boolean isBeanTypeSpecified = element.hasAttribute("beanType");
        boolean isBeanAnnotationSpecified = element.hasAttribute("beanAnnotation");

        if(!isBeanIdRefSpecified && !isBeanTypeSpecified && !isBeanAnnotationSpecified) {
        	throw new SmooksConfigurationException("One or more of attributes 'beanIdRef', 'beanType' and 'beanAnnotation' must be specified on a bean wiring configuration.");
        }
    }
}
