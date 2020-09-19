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
public final class ResourceConfigUtil {

    private ResourceConfigUtil() {
        
    }
    
    public static void setProperty(SmooksResourceConfiguration smooksResourceConfiguration, String setOn, Object value, Element xml, ExecutionContext executionContext) throws SmooksException {
        if(setOn.equals("selector")) {
            smooksResourceConfiguration.setSelector((String) value);
        } else if(setOn.equals("resource")) {
            smooksResourceConfiguration.setResource((String) value);
        } else if(setOn.equals("resourceType")) {
            smooksResourceConfiguration.setResourceType((String) value);
        } else if(setOn.equals("selector-namespace")) {
            smooksResourceConfiguration.getSelectorPath().setSelectorNamespaceURI((String) value);
        } else if(setOn.equals("defaultResource")) {
            smooksResourceConfiguration.setDefaultResource(Boolean.parseBoolean((String) value));
        } else if(setOn.equals("targetProfile")) {
            smooksResourceConfiguration.setTargetProfile((String) value);
        } else if(setOn.equals("condition") && ((String) value).length() > 0) {
            smooksResourceConfiguration.getSelectorPath().setConditionEvaluator(new BeanMapExpressionEvaluator((String) value));
        } else if(setOn.equals("conditionRef")) {
            ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
            smooksResourceConfiguration.getSelectorPath().setConditionEvaluator(extensionContext.getXmlConfigDigester().getConditionEvaluator((String) value));
        } else {
            Parameter<?> param = smooksResourceConfiguration.setParameter(setOn, value);
            if(xml != null) {
            	param.setXML(xml);
            }
        }
    }

    public static void setProperty(SmooksResourceConfiguration config, String setOn, Object value, ExecutionContext executionContext) throws SmooksException {
    	setProperty(config, setOn, value, null, executionContext);
    }

    public static void unsetProperty(SmooksResourceConfiguration smooksResourceConfiguration, String property) {
        if(property.equals("selector")) {
            smooksResourceConfiguration.setSelector(null);
        } else if(property.equals("resource")) {
            smooksResourceConfiguration.setResource(null);
        } else if(property.equals("resourceType")) {
            smooksResourceConfiguration.setResourceType(null);
        } else if(property.equals("selector-namespace")) {
            smooksResourceConfiguration.getSelectorPath().setSelectorNamespaceURI(null);
        } else if(property.equals("defaultResource")) {
            smooksResourceConfiguration.setDefaultResource(false);
        } else if(property.equals("targetProfile")) {
            smooksResourceConfiguration.setTargetProfile(null);
        } else if(property.equals("condition")) {
            smooksResourceConfiguration.getSelectorPath().setConditionEvaluator(null);
        } else if(property.equals("conditionRef")) {
            smooksResourceConfiguration.getSelectorPath().setConditionEvaluator(null);
        } else {
            smooksResourceConfiguration.removeParameter(property);
        }
    }

    public static void mapProperty(SmooksResourceConfiguration fromSmooksResourceConfiguration, String fromProperty, SmooksResourceConfiguration toSmooksResourcConfiguration, String toProperty, String defaultValue, ExecutionContext executionContext) throws SmooksException {
        if(fromProperty.equals("selector")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getSelectorPath().getSelector(), executionContext);
        } else if(fromProperty.equals("resource")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getResource(), executionContext);
        } else if(fromProperty.equals("resourceType")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getResourceType(), executionContext);
        } else if(fromProperty.equals("selector-namespace")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getSelectorPath().getSelectorNamespaceURI(), executionContext);
        } else if(fromProperty.equals("defaultResource")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.isDefaultResource(), executionContext);
        } else if(fromProperty.equals("targetProfile")) {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getTargetProfile(), executionContext);
        } else if(fromProperty.equals("condition")) {
            toSmooksResourcConfiguration.getSelectorPath().setConditionEvaluator(fromSmooksResourceConfiguration.getSelectorPath().getConditionEvaluator());
        } else if(fromProperty.equals("conditionRef")) {
            toSmooksResourcConfiguration.getSelectorPath().setConditionEvaluator(fromSmooksResourceConfiguration.getSelectorPath().getConditionEvaluator());
        } else {
            setProperty(toSmooksResourcConfiguration, toProperty, fromSmooksResourceConfiguration.getParameterValue(fromProperty, String.class, defaultValue), executionContext);
        }
    }
}
