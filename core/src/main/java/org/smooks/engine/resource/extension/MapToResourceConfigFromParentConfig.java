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
import org.smooks.api.SmooksConfigException;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.w3c.dom.Element;

import jakarta.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.EmptyStackException;
import java.util.Optional;
import java.util.Stack;

/**
 * Map a property value from a parent {@link ResourceConfig} and onto
 * the current {@link ResourceConfig}.
 * <p/>
 * The value is set on the {@link ResourceConfig} returned from the top
 * of the {@link org.smooks.engine.resource.extension.ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapToResourceConfigFromParentConfig implements DOMVisitBefore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapToResourceConfigFromParentConfig.class);

    @Inject
    private int parentRelIndex = -1;

    @Inject
    private String mapFrom;

    @Inject
    private Optional<String> mapTo;

    @Inject
    private Optional<String> defaultValue;

    @PostConstruct
    public void postConstruct() throws SmooksConfigException {
        if(parentRelIndex >= 0) {
            throw new SmooksConfigException("param 'parentRelIndex' value must be negative.  Value is '" + parentRelIndex + "'.");
        }
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        Stack<ResourceConfig> resourceStack = executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY).getResourceStack();
        ResourceConfig currentConfig;
        ResourceConfig parentConfig;

        String actualMapTo = mapTo.orElse(null);

        //If no mapTo is set then the mapFrom value becomes the mapTo value
        if(actualMapTo == null) {
        	actualMapTo = mapFrom;
        }

        // Get the current Config...
        try {
            currentConfig = resourceStack.peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No ResourceConfig available in ExtensionContext stack.  Unable to set ResourceConfig property '" + actualMapTo + "' with element text value.");
        }

        // Get the parent Config...
        try {
            parentConfig = resourceStack.get(resourceStack.size() - 1 + parentRelIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SmooksException("No Parent ResourceConfig available in ExtensionContext stack at relative index '" + parentRelIndex + "'.  Unable to set ResourceConfig property '" + actualMapTo + "' with value of '" + mapFrom + "' from parent configuration.");
        }

        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Mapping property '" + mapFrom + "' on parent resource configuration to property'" + actualMapTo + "'.");
        }
        ResourceConfigUtil.mapProperty(parentConfig, mapFrom, currentConfig, actualMapTo, defaultValue.orElse(null), executionContext);
    }
}
