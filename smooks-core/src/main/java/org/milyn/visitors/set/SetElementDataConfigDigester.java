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
package org.milyn.visitors.set;

import org.milyn.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.w3c.dom.Element;

/**
 * Digester for the {@link SetElementData}.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetElementDataConfigDigester implements DOMVisitAfter {

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        SmooksResourceConfiguration config = ExtensionContext.getExtensionContext(executionContext).getCurrentConfig();

        config.setParameter(new Parameter(SetElementData.ATTRIBUTE_DATA, "##value_as_xml_element").setXML(element));
    }
}
