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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.EmptyStackException;
import java.util.Optional;

/**
 * Map a property value onto the current {@link ResourceConfig} based on an
 * elements text content.
 * <p/>
 * The value is set on the {@link ResourceConfig} returned from the top
 * of the {@link ExtensionContext#getResourceStack() ExtensionContext resourece stack}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapToResourceConfigFromText implements DOMVisitBefore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapToResourceConfigFromText.class);

    @Inject
    private Optional<String> mapTo;

    @Inject
    private Optional<String> mapToSpecifier;

    @Inject
    private Optional<String> defaultValue;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ResourceConfig config;
        String value = DomUtils.getAllText(element, false);
        String mapToPropertyName = mapTo.orElse(null);

        if (mapToPropertyName == null) {
            if (!mapToSpecifier.isPresent()) {
                throw new SmooksException("One of attributes 'mapTo' or 'mapToSpecifier' must be specified.");
            }
            mapToPropertyName = DomUtils.getAttributeValue(element, mapToSpecifier.get());
        }

        try {
            config = executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY).getResourceStack().peek();
        } catch (EmptyStackException e) {
            throw new SmooksException("No ResourceConfig available in ExtensionContext stack.  Unable to set ResourceConfig property '" + mapToPropertyName + "' with element text value.");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Setting property '" + mapToPropertyName + "' on resource configuration to a value of '" + value + "'.");
        }

        ResourceConfigUtil.setProperty(config, mapToPropertyName, value, element, executionContext);
    }
}
