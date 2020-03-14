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
package org.smooks.cdr.extension;

import org.smooks.SmooksException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.AnnotationConstants;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Element;

/**
 * Create a new {@link org.smooks.cdr.SmooksResourceConfiguration} by cloning the current resource.
 * <p/>
 * The cloned {@link org.smooks.cdr.SmooksResourceConfiguration} is added to the {@link org.smooks.cdr.extension.ExtensionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CloneResourceConfig implements DOMElementVisitor {

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String resource;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String[] unset;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
        SmooksResourceConfiguration config = (SmooksResourceConfiguration) extensionContext.getResourceStack().peek().clone();

        if(unset != null) {
	        for(String property : unset) {
	            ResourceConfigUtil.unsetProperty(config, property);
	        }
        }

        if(resource != null) {
        	config.setResource(resource);
        }

        extensionContext.addResource(config);
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext.getExtensionContext(executionContext).getResourceStack().pop();
    }
}