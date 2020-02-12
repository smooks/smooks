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
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.xml.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.EmptyStackException;

/**
 * Map a property value onto the current {@link org.milyn.cdr.SmooksResourceConfiguration} based on an
 * elements text content.
 * <p/>
 * The value is set on the {@link org.milyn.cdr.SmooksResourceConfiguration} returned from the top
 * of the {@link ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapToResourceConfigFromText implements DOMVisitBefore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapToResourceConfigFromText.class);

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String mapTo;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String mapToSpecifier;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String defaultValue;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        SmooksResourceConfiguration config;
        String value = DomUtils.getAllText(element, false);
        String mapToPropertyName = mapTo;

        if(mapToPropertyName == null) {
            if(mapToSpecifier == null) {
                throw new SmooksException("One of attributes 'mapTo' or 'mapToSpecifier' must be specified.");
            }
            mapToPropertyName = DomUtils.getAttributeValue(element, mapToSpecifier);
        }

        try {
            config = ExtensionContext.getExtensionContext(executionContext).getResourceStack().peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No SmooksResourceConfiguration available in ExtensionContext stack.  Unable to set SmooksResourceConfiguration property '" + mapToPropertyName + "' with element text value.");
        }

        if (value == null) {
            value = defaultValue;
        }

        if (value == null) {
        	if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Not setting property '" + mapToPropertyName + "' on resource configuration.  Element '" + DomUtils.getName(element) + "' text value is null.  You may need to set a default value in the binding configuration.");
        	}
            return;
        } else {
        	if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Setting property '" + mapToPropertyName + "' on resource configuration to a value of '" + value + "'.");
        	}
        }

        ResourceConfigUtil.setProperty(config, mapToPropertyName, value, element, executionContext);
    }
}