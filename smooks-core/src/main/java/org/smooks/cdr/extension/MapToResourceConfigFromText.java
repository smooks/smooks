/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cdr.extension;

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AnnotationConstants;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.xml.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.EmptyStackException;

/**
 * Map a property value onto the current {@link org.smooks.cdr.SmooksResourceConfiguration} based on an
 * elements text content.
 * <p/>
 * The value is set on the {@link org.smooks.cdr.SmooksResourceConfiguration} returned from the top
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
