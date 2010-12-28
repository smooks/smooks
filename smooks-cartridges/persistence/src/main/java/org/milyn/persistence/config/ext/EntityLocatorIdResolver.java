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
package org.milyn.persistence.config.ext;

import org.milyn.SmooksException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.cdr.extension.ResourceConfigUtil;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class EntityLocatorIdResolver implements DOMVisitBefore {

	@AppContext
	private ApplicationContext applicationContext;

	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext)
			throws SmooksException {
		SmooksResourceConfiguration config = ExtensionContext.getExtensionContext(executionContext).getResourceStack().peek();

		int index = LocatorIndex.getLocatorIndex(applicationContext).increment();

		ResourceConfigUtil.setProperty(config, "id", Integer.toString(index), executionContext);
	}

}
