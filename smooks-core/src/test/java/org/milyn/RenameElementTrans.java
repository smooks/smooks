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

package org.milyn;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Element;

public class RenameElementTrans implements DOMElementVisitor {

    // cache the new element name.
    private String newElementName;

    public RenameElementTrans() {
    }

    public RenameElementTrans(String newElementName) {
        this.newElementName = newElementName;
    }

    public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
        // Capture the new name for the element from the configuration...
        newElementName = resourceConfig.getStringParameter("new-name");
    }

    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
        // Rename the element to the configured new name.
        DomUtils.renameElement(element, newElementName, true, true);
    }
}
