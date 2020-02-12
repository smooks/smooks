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
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Map a property value from a parent {@link org.milyn.cdr.SmooksResourceConfiguration} and onto
 * the current {@link org.milyn.cdr.SmooksResourceConfiguration}.
 * <p/>
 * The value is set on the {@link org.milyn.cdr.SmooksResourceConfiguration} returned from the top
 * of the {@link org.milyn.cdr.extension.ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapToResourceConfigFromParentConfig implements DOMVisitBefore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapToResourceConfigFromText.class);

    @ConfigParam(defaultVal = "-1")
    private int parentRelIndex;

    @ConfigParam
    private String mapFrom;

    @ConfigParam(use = Use.OPTIONAL)
    private String mapTo;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String defaultValue;

    @Initialize
    public void initialize() throws SmooksConfigurationException {
        if(parentRelIndex >= 0) {
            throw new SmooksConfigurationException("param 'parentRelIndex' value must be negative.  Value is '" + parentRelIndex + "'.");
        }
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        Stack<SmooksResourceConfiguration> resourceStack = ExtensionContext.getExtensionContext(executionContext).getResourceStack();
        SmooksResourceConfiguration currentConfig;
        SmooksResourceConfiguration parentConfig;
        String value;

        String actualMapTo = mapTo;

        //If no mapTo is set then the mapFrom value becomes the mapTo value
        if(actualMapTo == null) {
        	actualMapTo = mapFrom;
        }

        // Get the current Config...
        try {
            currentConfig = resourceStack.peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No SmooksResourceConfiguration available in ExtensionContext stack.  Unable to set SmooksResourceConfiguration property '" + actualMapTo + "' with element text value.");
        }

        // Get the parent Config...
        try {
            parentConfig = resourceStack.get(resourceStack.size() - 1 + parentRelIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SmooksException("No Parent SmooksResourceConfiguration available in ExtensionContext stack at relative index '" + parentRelIndex + "'.  Unable to set SmooksResourceConfiguration property '" + actualMapTo + "' with value of '" + mapFrom + "' from parent configuration.");
        }

        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Mapping property '" + mapFrom + "' on parent resource configuration to property'" + actualMapTo + "'.");
        }
        ResourceConfigUtil.mapProperty(parentConfig, mapFrom, currentConfig, actualMapTo, defaultValue, executionContext);
    }
}