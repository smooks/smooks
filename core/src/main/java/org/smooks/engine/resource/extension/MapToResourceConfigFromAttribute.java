/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.resource.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.EmptyStackException;
import java.util.Optional;

/**
 * Map a property value onto the current {@link ResourceConfig} based on an
 * element attribute value.
 * <p/>
 * The value is set on the {@link ResourceConfig} returned from the top
 * of the {@link org.smooks.engine.resource.extension.ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapToResourceConfigFromAttribute implements DOMVisitBefore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapToResourceConfigFromAttribute.class);

    @Inject
    private Optional<String> mapTo;

    @Inject
    private Optional<String> mapToSpecifier;

    @Inject
    private String attribute;

    @Inject
    private Optional<String> defaultValue;

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ResourceConfig resourceConfig;
        String value = DomUtils.getAttributeValue(element, attribute);

        String actualMapTo = mapTo.orElse(null);

        if (actualMapTo == null && mapToSpecifier.isPresent()) {
            actualMapTo = DomUtils.getAttributeValue(element, mapToSpecifier.get());
        }

        //If no mapTo is set then the attribute value becomes the mapTo value
        if (actualMapTo == null) {
            actualMapTo = attribute;
        }

        try {
            resourceConfig = executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY).getResourceStack().peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No ResourceConfig available in ExtensionContext stack.  Unable to set ResourceConfig property '" + actualMapTo + "' with attribute '" + attribute + "' value '" + value + "'.");
        }

        if (value == null) {
            value = defaultValue.orElse(null);
        }

        if (value == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not setting property '" + actualMapTo + "' on resource configuration.  Attribute '" + attribute + "' value on element '" + DomUtils.getName(element) + "' is null.  You may need to set a default value in the binding configuration.");
            }
            return;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Setting property '" + actualMapTo + "' on resource configuration to a value of '" + value + "'.");
            }
        }

        ResourceConfigUtil.setProperty(resourceConfig, actualMapTo, value, executionContext);
    }
}
