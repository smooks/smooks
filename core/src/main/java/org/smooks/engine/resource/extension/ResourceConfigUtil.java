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
package org.smooks.engine.resource.extension;

import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.config.Parameter;
import org.w3c.dom.Element;

/**
 * Resource Configuration Extension utility class.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public final class ResourceConfigUtil {

    private ResourceConfigUtil() {
        
    }
    
    public static void setProperty(ResourceConfig resourceConfig, String setOn, Object value, Element xml, ExecutionContext executionContext) throws SmooksException {
        if(setOn.equals("selector")) {
            resourceConfig.setSelector((String) value);
        } else if(setOn.equals("resource")) {
            resourceConfig.setResource((String) value);
        } else if(setOn.equals("resourceType")) {
            resourceConfig.setResourceType((String) value);
        } else if(setOn.equals("selector-namespace")) {
            resourceConfig.getSelectorPath().setSelectorNamespaceURI((String) value);
        } else if(setOn.equals("defaultResource")) {
            resourceConfig.setDefaultResource(Boolean.parseBoolean((String) value));
        } else if(setOn.equals("targetProfile")) {
            resourceConfig.setTargetProfile((String) value);
        } else if(setOn.equals("conditionRef")) {
            ExtensionContext extensionContext = executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY);
            resourceConfig.getSelectorPath().setConditionEvaluator(extensionContext.getXmlConfigDigester().getConditionEvaluator((String) value));
        } else {
            Parameter<?> param = resourceConfig.setParameter(setOn, value);
            if(xml != null) {
            	param.setXml(xml);
            }
        }
    }

    public static void setProperty(ResourceConfig resourceConfig, String setOn, Object value, ExecutionContext executionContext) throws SmooksException {
    	setProperty(resourceConfig, setOn, value, null, executionContext);
    }

    public static void unsetProperty(ResourceConfig resourceConfig, String property) {
        if(property.equals("selector")) {
            resourceConfig.setSelector(null);
        } else if(property.equals("resource")) {
            resourceConfig.setResource(null);
        } else if(property.equals("resourceType")) {
            resourceConfig.setResourceType(null);
        } else if(property.equals("selector-namespace")) {
            resourceConfig.getSelectorPath().setSelectorNamespaceURI(null);
        } else if(property.equals("defaultResource")) {
            resourceConfig.setDefaultResource(false);
        } else if(property.equals("targetProfile")) {
            resourceConfig.setTargetProfile(null);
        } else if(property.equals("condition")) {
            resourceConfig.getSelectorPath().setConditionEvaluator(null);
        } else if(property.equals("conditionRef")) {
            resourceConfig.getSelectorPath().setConditionEvaluator(null);
        } else {
            resourceConfig.removeParameter(property);
        }
    }

    public static void mapProperty(ResourceConfig fromResourceConfig, String fromProperty, ResourceConfig toResourceConfig, String toProperty, String defaultValue, ExecutionContext executionContext) throws SmooksException {
        if(fromProperty.equals("selector")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getSelectorPath().getSelector(), executionContext);
        } else if(fromProperty.equals("resource")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getResource(), executionContext);
        } else if(fromProperty.equals("resourceType")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getResourceType(), executionContext);
        } else if(fromProperty.equals("selector-namespace")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getSelectorPath().getSelectorNamespaceURI(), executionContext);
        } else if(fromProperty.equals("defaultResource")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.isDefaultResource(), executionContext);
        } else if(fromProperty.equals("targetProfile")) {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getTargetProfile(), executionContext);
        } else if(fromProperty.equals("condition")) {
            toResourceConfig.getSelectorPath().setConditionEvaluator(fromResourceConfig.getSelectorPath().getConditionEvaluator());
        } else if(fromProperty.equals("conditionRef")) {
            toResourceConfig.getSelectorPath().setConditionEvaluator(fromResourceConfig.getSelectorPath().getConditionEvaluator());
        } else {
            setProperty(toResourceConfig, toProperty, fromResourceConfig.getParameterValue(fromProperty, String.class, defaultValue), executionContext);
        }
    }
}
