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

package org.milyn.cdres.trans;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Element;

/**
 * Renames an attribute from a DOM element <u>during the processing phase</u>.
 * <h3>.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource	useragent="<i>device/profile</i>" selector="<i>target-element-name</i>" 
 * 	path="org.milyn.cdres.trans.RenameAttributeTU"&gt;
 * 
 * 	&lt;!-- The name of the element attribute to be renamed. --&gt;
 * 	&lt;param name="<b>attributeName</b>"&gt;<i>attribute-name</i>&lt;/param&gt;
 * 
 * 	&lt;!-- The new name of the element attribute. --&gt;
 * 	&lt;param name="<b>attributeNewName</b>"&gt;<i>attribute-new-name</i>&lt;/param&gt;
 * 
 * 	&lt;!-- (Optional) Overwrite existing attributes of the same name. Default false. --&gt;
 * 	&lt;param name="<b>overwrite</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * 
 * 	&lt;!-- (Optional) Visit the target element before iterating over the elements
 * 		child content. Default false. --&gt;
 * 	&lt;param name="<b>visitBefore</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * &lt;/smooks-resource&gt;</pre>
 * See {@link org.milyn.cdr.SmooksResourceConfiguration}.
 * @author tfennelly
 */
public class RenameAttributeTU implements DOMElementVisitor {

    @ConfigParam
    private String attributeName;

    @ConfigParam
    private String attributeNewName;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL, defaultVal = "false")
    private boolean overwrite;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL, defaultVal = "false")
	private boolean visitBefore;
	
    public void visitBefore(Element element, ExecutionContext executionContext) {
        if(visitBefore) {
            visit(element, executionContext);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) {
        if(!visitBefore) {
            visit(element, executionContext);
        }
    }

    private void visit(Element element, ExecutionContext executionContext) {
		String attributeValue;
		
		// If the element already has the new attribute and we're
		// not overwriting, leave all as is and return.
		if(!overwrite && element.hasAttribute(attributeNewName)) {
			return;
		}
		
		attributeValue = element.getAttribute(attributeName);
		if(attributeValue != null) {
			element.removeAttribute(attributeName);
			element.setAttribute(attributeNewName, attributeValue);
		}
	}
}
