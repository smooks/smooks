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

    public static void setProperty(SmooksResourceConfiguration config, String setOn, String value, Element xml, ExecutionContext executionContext) throws SmooksException {
        if(setOn.equals("selector")) {
            config.setSelector(value);
        } else if(setOn.equals("resource")) {
            config.setResource(value);
        } else if(setOn.equals("resourceType")) {
            config.setResourceType(value);
        } else if(setOn.equals("selector-namespace")) {
            config.setSelectorNamespaceURI(value);
        } else if(setOn.equals("defaultResource")) {
            config.setDefaultResource(Boolean.parseBoolean(value));
        } else if(setOn.equals("targetProfile")) {
            config.setTargetProfile(value);
        } else if(setOn.equals("condition") && value.length() > 0) {
            config.setConditionEvaluator(new BeanMapExpressionEvaluator(value));
        } else if(setOn.equals("conditionRef")) {
            ExtensionContext execentionContext = ExtensionContext.getExtensionContext(executionContext);
            config.setConditionEvaluator(execentionContext.getXmlConfigDigester().getConditionEvaluator(value));
        } else {
            Parameter param = config.setParameter(setOn, value);
            if(xml != null) {
            	param.setXML(xml);
            }
        }
    }

    public static void setProperty(SmooksResourceConfiguration config, String setOn, String value, ExecutionContext executionContext) throws SmooksException {
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
            setProperty(toConfig, toProperty, Boolean.toString(fromConfig.isDefaultResource()), executionContext);
        } else if(fromProperty.equals("targetProfile")) {
            setProperty(toConfig, toProperty, fromConfig.getTargetProfile(), executionContext);
        } else if(fromProperty.equals("condition")) {
            toConfig.setConditionEvaluator(fromConfig.getConditionEvaluator());
        } else if(fromProperty.equals("conditionRef")) {
            toConfig.setConditionEvaluator(fromConfig.getConditionEvaluator());
        } else {
            setProperty(toConfig, toProperty, fromConfig.getStringParameter(fromProperty, defaultValue), executionContext);
        }
    }
}