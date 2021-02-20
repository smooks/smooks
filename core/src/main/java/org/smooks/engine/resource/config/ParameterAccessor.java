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
package org.smooks.engine.resource.config;

import org.smooks.Smooks;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.ExecutionContext;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.ResourceConfigListsLookup;

import java.util.List;
import java.util.Map;

/**
 * Accessor class for looking up global parameters.
 * <p id="decode"/>
 * Profile specific parameters are stored under the "global-parameters" selector
 * (see {@link ResourceConfig}).  The parameter values are
 * stored in the &lt;param&gt; elements within this Content Delivery Resource definition.
 * This class iterates over the list of {@link ResourceConfig}
 * elements targeted at the {@link ExecutionContext} profile.  It looks for a definition of the named
 * parameter.  If the &lt;param&gt; has a type attribute the 
 * {@link ParameterDecoder} for that type can be applied to the attribute
 * value through the {@link #getParameterObject(String, ContentDeliveryConfig)} method,
 * returning whatever Java type defined by the {@link ParameterDecoder}
 * implementation.  As an example, see {@link TokenizedStringParameterDecoder}.
 * 
 * @author tfennelly
 */
public abstract class ParameterAccessor {
	
	/**
	 * Device parameters .cdrl lookup string.
	 */
	public static final String GLOBAL_PARAMETERS = "global-parameters";
	
	/**
	 * Get the named parameter String value.
	 * @param name Name of parameter to get. 
	 * @param config The {@link ContentDeliveryConfig} for the requesting device.
	 * @return Parameter value, or null if not set.
	 */
	public static <T> T getParameterValue(String name, Class<T> valueClass, ContentDeliveryConfig config) {
		Parameter<T> param = getParameter(name, valueClass, config);
		
		if(param != null) {
			return param.getValue();
		}
		
		return null;
	}

	/**
	 * Get the named parameter String value.
	 * @param name Name of parameter to get. 
	 * @param defaultVal Default value returned if the parameter is not defined.
	 * @param config The {@link ContentDeliveryConfig} for the requesting device.
	 * @return Parameter value, or null if not set.
	 */
	public static <T> T getParameterValue(String name,  Class<T> valueClass, T defaultVal, ContentDeliveryConfig config) {
		Parameter<T> param = getParameter(name, valueClass, config);
		
		if(param != null) {
			return param.getValue();
		}
		
		return defaultVal;
	}
	
    /**
	 * Get the named parameter.
     * <p/>
     * Calls {@link ContentDeliveryConfig#getResourceConfigs()}
     * to get the configurations map and then passes that to
     * {@link #getParameter(String, Class valueType, java.util.Map)}, returning its return value.
     *
	 * @param name Parameter name.
	 * @param config Device Delivery Configuration.
	 * @return The Parameter instance for the named parameter, or null if not defined.
	 */
	public static <T> Parameter<T> getParameter(String name, Class<T> valueType, ContentDeliveryConfig config) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(config, "config");

        return getParameter(name, valueType, config.getResourceConfigs());
	}

    /**
     * Get the named parameter from the supplied resource config map.
     *
     * @param name The parameter name.
     * @param resourceConfigsBySelector The resource configuration map.
     * @return The parameter value, or null if not found.
     */
    public static <T> Parameter<T> getParameter(String name, Class<T> valueType, Map<String, List<ResourceConfig>> resourceConfigsBySelector) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(resourceConfigsBySelector, "resourceConfigsBySelector");
        List<ResourceConfig> resourceConfigs = resourceConfigsBySelector.get(GLOBAL_PARAMETERS);

        if(resourceConfigs != null) {
            for (ResourceConfig resourceConfig : resourceConfigs) {
                Parameter<T> param = resourceConfig.getParameter(name, valueType);
                if(param != null) {
                    return param;
                }
            }
        }

        // Check the System properties...
        T systemValue = (T) System.getProperty(name);
        if(systemValue != null) {
            return new DefaultParameter<>(name, systemValue);
        }

        return null;
    }

    public static <T> T getParameterValue(String name, Class<T> valueType, T defaultVal, Map<String, List<ResourceConfig>> config) {
        Parameter<T> param = getParameter(name, valueType, config);

        if(param != null) {
            return param.getValue();
        }

        return defaultVal;
    }
    
    /**
     * Get the named parameter from the supplied resource config map.
     *
     * @param name The parameter name.
     * @param resourceConfigsBySelector The resource configuration map.
     * @return The parameter value, or null if not found.
     */
 
     public static <T> T getParameterValue(String name, Class<T> valueType, Map<String, List<ResourceConfig>> resourceConfigsBySelector) {
        Parameter<T> parameter = getParameter(name, valueType, resourceConfigsBySelector);

        if(parameter != null) {
            return parameter.getValue();
        }

        return null;
    }

    public static void setParameter(String name, Object value, Smooks smooks) {
        ResourceConfig resourceConfig = new DefaultResourceConfig(ParameterAccessor.GLOBAL_PARAMETERS);

        resourceConfig.setParameter(name, value);
        smooks.getApplicationContext().getRegistry().registerResourceConfig(resourceConfig);
    }

    public static void removeParameter(String name, Smooks smooks) {
        Registry registry = smooks.getApplicationContext().getRegistry();

        for (ResourceConfigSeq list : registry.lookup(new ResourceConfigListsLookup())) {
            for (int i = 0; i < list.size(); i++) {
                ResourceConfig nextResourceConfig = list.get(i);
                if (ParameterAccessor.GLOBAL_PARAMETERS.equals(nextResourceConfig.getSelectorPath().getSelector())) {
                    nextResourceConfig.removeParameter(name);
                }
            }
        }
    }
}
