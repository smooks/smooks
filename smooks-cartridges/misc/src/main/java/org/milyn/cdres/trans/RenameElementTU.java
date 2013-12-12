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
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.commons.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Renames/replaces an element in the document <u>during the processing phase</u>.
 * <p/>
 * The element is visited by this Processing Unit after it's child content
 * has been iterated over.
 * <p/>
 * See {@link DomUtils#renameElement(org.w3c.dom.Element, java.lang.String, boolean, boolean)}.
 * 
 * <h3>.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource	useragent="<i>device/profile</i>" selector="<i>target-element-name</i>" path="org.milyn.cdres.trans.RenameElementTU"&gt;
 * 
 * 	&lt;!-- The name of the replacement element. --&gt;
 * 	&lt;param name="<b>replacementElement</b>"&gt;<i>replacement-element-name</i>&lt;/param&gt;
 * 
 * 	&lt;!-- (Optional) Copy target elements child content to the replacement 
 * 		element. Default is true. --&gt;
 * 	&lt;param name="<b>keepChildContent</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * 
 * 	&lt;!-- (Optional) Copy target elements attributes to the replacement 
 * 		element. Default is true. --&gt;
 * 	&lt;param name="<b>keepAttributes</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * &lt;/smooks-resource&gt;</pre>
 * See {@link org.milyn.cdr.SmooksResourceConfiguration}.
 * 
 * @author tfennelly
 */
public class RenameElementTU implements DOMElementVisitor {

    @ConfigParam
    private String replacementElement;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL, defaultVal = "true")
	private boolean keepChildContent;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL, defaultVal = "true")
	private boolean keepAttributes;

    public void visitBefore(Element element, ExecutionContext executionContext) {
    }

	public void visitAfter(Element element, ExecutionContext request) {
		DomUtils.renameElement(element, replacementElement, keepChildContent, keepAttributes);
	}
}
