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

package org.milyn.smooks.scripting;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;

public class MyGroovyScript implements DOMElementVisitor {

	String newName;
	
	public void setConfiguration(SmooksResourceConfiguration config) {
		newName = config.getStringParameter("new-name", "zzz");
	}

	public void visitBefore(Element fragment, ExecutionContext context) {
	}

	public void visitAfter(Element fragment, ExecutionContext context) {
		DomUtils.renameElement(fragment, newName, true, true);
	}
}
