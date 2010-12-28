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
package org.milyn.cdr.extension;

import org.milyn.SmooksException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Create a new {@link SmooksResourceConfiguration}.
 * <p/>
 * The new {@link SmooksResourceConfiguration} is added to the {@link ExtensionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NewResourceConfig implements DOMElementVisitor {

	public static final String PARAMETER_TARGET_PROFILE = "targetProfile";

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String resource;

    @ConfigParam(defaultVal = "false")
    private boolean isTemplate;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);

        config.setExtendedConfigNS(element.getNamespaceURI());
        config.setResource(resource);

        // Set the defaults...
        if(extensionContext.getDefaultSelector() != null) {
            config.setSelector(extensionContext.getDefaultSelector());
        }
        config.setSelectorNamespaceURI(extensionContext.getDefaultNamespace());

        String targetProfile = DomUtils.getAttributeValue(element, PARAMETER_TARGET_PROFILE);
        if(targetProfile == null) {
        	targetProfile = extensionContext.getDefaultProfile();
        }
        config.setTargetProfile(targetProfile);
        config.setConditionEvaluator(extensionContext.getDefaultConditionEvaluator());

        if(isTemplate) {
        	extensionContext.addResourceTemplate(config);
        } else {
        	extensionContext.addResource(config);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext.getExtensionContext(executionContext).getResourceStack().pop();
    }
}
