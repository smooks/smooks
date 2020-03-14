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

import java.util.List;

import org.smooks.SmooksException;
import org.smooks.cdr.ConfigSearch;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.extension.ExtensionContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.javabean.BeanInstanceCreator;
import org.w3c.dom.Element;

/**
 * Configure the "retain" bean attribute, if not configured by the
 * user.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetRetainParam implements DOMVisitBefore {

	/* (non-Javadoc)
	 * @see org.smooks.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.smooks.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
        
        // The current config on the stack must be a <jb:bean>...
        SmooksResourceConfiguration beanConfig = (SmooksResourceConfiguration) extensionContext.getResourceStack().peek();
        String retain = beanConfig.getStringParameter("retain");
		
        // If the "retain" attribute is not configured we configure it.  If
        // this is the first bean config, we set it to "true" (i.e. retain it),
        // otherwise set it to "false" (i.e. do not retain it)...
        if(retain == null) {
        	List<SmooksResourceConfiguration> creatorConfigs = extensionContext.lookupResource(new ConfigSearch().resource(BeanInstanceCreator.class.getName()));

        	if(!creatorConfigs.isEmpty()) {
        		// This is not the first bean config... set retain to "false"
        		beanConfig.setParameter("retain", "false");
        	}
        }
	}
}
