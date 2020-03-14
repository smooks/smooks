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

package org.smooks.cdres.trans;

import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Removes a DOM element <u>during the processing phase</u>.
 * <p/>
 * The element is visited by this Processing Unit after it's child content
 * has been iterated over.
 * <p/>
 * See {@link DomUtils#removeElement(org.w3c.dom.Element, boolean)}.
 * 
 * <h3>.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource	useragent="<i>device/profile</i>" selector="<i>target-element-name</i>" path="org.smooks.cdres.trans.RemoveElementTU" &gt;
 * 
 * 	&lt;!-- (Optional) Keep child content. Default is true. --&gt;
 * 	&lt;param name="<b>keepChildContent</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * 
 * &lt;/smooks-resource&gt;</pre>
 * 
 * See {@link org.smooks.cdr.SmooksResourceConfiguration}.
 * @author tfennelly
 */
public class RemoveElementTU implements DOMElementVisitor {

    @ConfigParam(use = ConfigParam.Use.OPTIONAL, defaultVal = "true")
	private boolean keepChildContent;

    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

	/* (non-Javadoc)
	 * @see org.smooks.delivery.dom.DOMElementVisitor#visitAfter(org.w3c.dom.Element, org.smooks.container.ExecutionContext)
	 */
	public void visitAfter(Element element, ExecutionContext request) {
		DomUtils.removeElement(element, keepChildContent);
	}
}
