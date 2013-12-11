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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

import java.util.EmptyStackException;

/**
 * Set a static value on the current {@link org.milyn.cdr.SmooksResourceConfiguration}.
 * <p/>
 * The value is set on the {@link org.milyn.cdr.SmooksResourceConfiguration} returned from the top
 * of the {@link org.milyn.cdr.extension.ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetOnResourceConfig implements DOMVisitBefore {

    private static Log logger = LogFactory.getLog(MapToResourceConfigFromText.class);

    @ConfigParam
    private String setOn;

    @ConfigParam
    private String value;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        SmooksResourceConfiguration config;

        try {
            config = ExtensionContext.getExtensionContext(executionContext).getResourceStack().peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No SmooksResourceConfiguration available in ExtensionContext stack.  Unable to set SmooksResourceConfiguration property '" + setOn + "' with static value.");
        }

        logger.debug("Setting property '" + setOn + "' on resource configuration to a value of '" + value + "'.");

        ResourceConfigUtil.setProperty(config, setOn, value, executionContext);
    }
}