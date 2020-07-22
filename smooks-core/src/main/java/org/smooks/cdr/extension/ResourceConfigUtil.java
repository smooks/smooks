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
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.javabean.expression.BeanMapExpressionEvaluator;
import org.w3c.dom.Element;

/**
 * Resource Configuration Extension utility class.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class ResourceConfigUtil {

    public static void setProperty(SmooksResourceConfiguration config, String setOn, Object value, Element xml, ExecutionContext executionContext) throws SmooksException {
        if(setOn.equals("selector")) {
            config.setSelector((String) value);
        } else if(setOn.equals("resource")) {
            config.setResource((String) value);
        } else if(setOn.equals("resourceType")) {
            config.setResourceType((String) value);
        } else if(setOn.equals("selector-namespace")) {
            config.setSelectorNamespaceURI((String) value);
        } else if(setOn.equals("defaultResource")) {
            config.setDefaultResource(Boolean.parseBoolean((String) value));
        } else if(setOn.equals("targetProfile")) {
            config.setTargetProfile((String) value);
        } else if(setOn.equals("condition") && ((String) value).length() > 0) {
            config.setConditionEvaluator(new BeanMapExpressionEvaluator((String) value));
        } else if(setOn.equals("conditionRef")) {
            ExtensionContext execentionContext = ExtensionContext.getExtensionContext(executionContext);
            config.setConditionEvaluator(execentionContext.getXmlConfigDigester().getConditionEvaluator((String) value));
        } else {
            Parameter<?> param = config.setParameter(setOn, value);
            if(xml != null) {
            	param.setXML(xml);
            }
        }
    }

    public static void setProperty(SmooksResourceConfiguration config, String setOn, Object value, ExecutionContext executionContext) throws SmooksException {
    	setProperty(config, setOn, value, null, executionContext);
    }

    public static void unsetProperty(SmooksResourceConfiguration config, String property) {
        if(property.equals("selector")) {
            config.setSelector(null);
        } else if(property.equals("resource")) {
            config.setResource(null);
        } else if(property.equals("resourceType")) {
            config.setResourceType(null);
        } else if(property.equals("selector-namespace")) {
            config.setSelectorNamespaceURI(null);
        } else if(property.equals("defaultResource")) {
            config.setDefaultResource(false);
        } else if(property.equals("targetProfile")) {
            config.setTargetProfile(null);
        } else if(property.equals("condition")) {
            config.setConditionEvaluator(null);
        } else if(property.equals("conditionRef")) {
            config.setConditionEvaluator(null);
        } else {
            config.removeParameter(property);
        }
    }

    public static void mapProperty(SmooksResourceConfiguration fromConfig, String fromProperty, SmooksResourceConfiguration toConfig, String toProperty, String defaultValue, ExecutionContext executionContext) throws SmooksException {
        if(fromProperty.equals("selector")) {
            setProperty(toConfig, toProperty, fromConfig.getSelector(), executionContext);
        } else if(fromProperty.equals("resource")) {
            setProperty(toConfig, toProperty, fromConfig.getResource(), executionContext);
        } else if(fromProperty.equals("resourceType")) {
            setProperty(toConfig, toProperty, fromConfig.getResourceType(), executionContext);
        } else if(fromProperty.equals("selector-namespace")) {
            setProperty(toConfig, toProperty, fromConfig.getSelectorNamespaceURI(), executionContext);
        } else if(fromProperty.equals("defaultResource")) {
            setProperty(toConfig, toProperty, fromConfig.isDefaultResource(), executionContext);
        } else if(fromProperty.equals("targetProfile")) {
            setProperty(toConfig, toProperty, fromConfig.getTargetProfile(), executionContext);
        } else if(fromProperty.equals("condition")) {
            toConfig.setConditionEvaluator(fromConfig.getConditionEvaluator());
        } else if(fromProperty.equals("conditionRef")) {
            toConfig.setConditionEvaluator(fromConfig.getConditionEvaluator());
        } else {
            setProperty(toConfig, toProperty, fromConfig.getParameterValue(fromProperty, String.class, defaultValue), executionContext);
        }
    }
}
