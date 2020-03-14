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

import org.apache.commons.lang.StringUtils;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class WireOnElementChecker implements DOMVisitBefore {

	/* (non-Javadoc)
	 * @see org.smooks.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.smooks.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext)
			throws SmooksException {

		if(!isCreateOnElementSet(element) && !isWireOnElementSet(element)) {
			throw new SmooksConfigurationException("The bindings attribute 'createOnElement' and wiring attribute 'wireOnElement' " +
					"are both not set. One of them must at least be set. If the result of this binding should be a new populated Object " +
					"then you need to set the 'createOnElement' bindings attribute. If you want to update an existing object in the bean " +
					"context then you must set the 'wireOnElement' attribute.");
		}

	}

	private boolean isCreateOnElementSet(Element element) {
		return StringUtils.isNotEmpty(((Element)element.getParentNode()).getAttribute("createOnElement"));
    }

	private boolean isWireOnElementSet(Element element) {
        return StringUtils.isNotEmpty(element.getAttribute("wireOnElement"));
    }


}
