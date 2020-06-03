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
package org.smooks.cdr;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.assertion.AssertArgument;
import org.smooks.Smooks;
import org.smooks.SmooksUtil;

/**
 * Accessor class for looking up global parameters.
 * <p id="decode"/>
 * Profile specific parameters are stored under the "global-parameters" selector
 * (see {@link org.smooks.cdr.SmooksResourceConfiguration}).  The parameter values are
 * stored in the &lt;param&gt; elements within this Content Delivery Resource definition.
 * This class iterates over the list of {@link org.smooks.cdr.SmooksResourceConfiguration}
 * elements targeted at the {@link org.smooks.container.ExecutionContext} profile.  It looks for a definition of the named
 * parameter.  If the &lt;param&gt; has a type attribute the 
 * {@link org.smooks.cdr.ParameterDecoder} for that type can be applied to the attribute
 * value through the {@link #getParameterObject(String,org.smooks.delivery.ContentDeliveryConfig)} method,
 * returning whatever Java type defined by the {@link org.smooks.cdr.ParameterDecoder}
 * implementation.  As an example, see {@link org.smooks.cdr.TokenizedStringParameterDecoder}.
 * 
 * @author tfennelly
 */
public abstract class ParameterAccessor {
	
	/**
	 * Device parameters .cdrl lookup string.
	 */
	public static final String GLOBAL_PARAMETERS = "global-parameters";

	/**
	 * Get the named parameter instance (decode).
	 * @param name Parameter name.
	 * @param config Device Delivery Configuration.
	 * @return The Parameter instance for the named parameter (<a href="#decode">decoded to an Object</a>), 
	 * or null if not defined.
	 */
	public static Object getParameterObject(String name, ContentDeliveryConfig config) {
		Parameter param = getParamter(name, config);
		
		if(param != null) {
			return param.getValue(config);
		}
		
		return null;
	}

	/**
	 * Get the named parameter String value.
	 * @param name Name of parameter to get. 
	 * @param config The {@link ContentDeliveryConfig} for the requesting device.
	 * @return Parameter value, or null if not set.
	 */
	public static String getStringParameter(String name, ContentDeliveryConfig config) {
		Parameter param = getParamter(name, config);
		
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
	public static String getStringParameter(String name, String defaultVal, ContentDeliveryConfig config) {
		Parameter param = getParamter(name, config);
		
		if(param != null) {
			return param.getValue();
		}
		
		return defaultVal;
	}

	/**
	 * Get the named SmooksResourceConfiguration parameter as a boolean.
	 * @param name Name of parameter to get. 
	 * @param defaultVal The default value to be returned if there are no 
	 * parameters on the this SmooksResourceConfiguration instance, or the parameter is not defined.
	 * @param config The {@link ContentDeliveryConfig} for the requesting device.
	 * @return true if the parameter is set to true, defaultVal if not defined, otherwise false.
	 */
	public static boolean getBoolParameter(String name, boolean defaultVal, ContentDeliveryConfig config) {
		Parameter param = getParamter(name, config);
        return toBoolean(param, defaultVal);
	}

    /**
     * Get the named SmooksResourceConfiguration parameter as a boolean.
     * @param name Name of parameter to get.
     * @param defaultVal The default value to be returned if there are no
     * parameters on the this SmooksResourceConfiguration instance, or the parameter is not defined.
     * @param config The config map.
     * @return true if the parameter is set to true, defaultVal if not defined, otherwise false.
     */
    public static boolean getBoolParameter(String name, boolean defaultVal, Map<String, List<SmooksResourceConfiguration>> config) {
        Parameter param = getParameter(name, config);
        return toBoolean(param, defaultVal);
    }

    private static boolean toBoolean(Parameter param, boolean defaultVal) {
        String paramVal;

        if(param == null) {
            return defaultVal;
        }

        paramVal = param.getValue();
        if(paramVal == null) {
            return defaultVal;
        }
        paramVal = paramVal.trim();
        if(paramVal.equals("true")) {
            return true;
        } else if(paramVal.equals("false")) {
            return false;
        } else {
            return defaultVal;
        }
    }

    /**
	 * Get the named parameter.
     * <p/>
     * Calls {@link org.smooks.delivery.ContentDeliveryConfig#getSmooksResourceConfigurations()}
     * to get the configurations map and then passes that to
     * {@link #getParameter(String, java.util.Map)}, returning its return value.
     *
	 * @param name Parameter name.
	 * @param config Device Delivery Configuration.
	 * @return The Parameter instance for the named parameter, or null if not defined.
	 */
	public static Parameter getParamter(String name, ContentDeliveryConfig config) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(config, "config");

        return getParameter(name, config.getSmooksResourceConfigurations());
	}

    /**
     * Get the named parameter from the supplied resource config map.
     *
     * @param name The parameter name.
     * @param resourceConfigurations The resource configuration map.
     * @return The parameter value, or null if not found.
     */
    public static Parameter getParameter(String name, Map<String, List<SmooksResourceConfiguration>> resourceConfigurations) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(resourceConfigurations, "resourceConfigurations");
        List<SmooksResourceConfiguration> configList = resourceConfigurations.get(GLOBAL_PARAMETERS);

        if(configList != null) {
            // Backward compatibility...
            List<SmooksResourceConfiguration> cbConfigList = resourceConfigurations.get("device-parameters");
            if(cbConfigList != null) {
                configList.addAll(cbConfigList);
            }

            for (SmooksResourceConfiguration resourceConfig : configList) {
                Parameter param = resourceConfig.getParameter(name);
                if(param != null) {
                    return param;
                }
            }
        }

        // Check the System properties...
        String systemValue = System.getProperty(name);
        if(systemValue != null) {
            return new Parameter(name, systemValue);
        }

        return null;
    }

    /**
     * Get the named parameter from the supplied resource config map.
     *
     * @param name The parameter name.
     * @param resourceConfigurations The resource configuration map.
     * @return The parameter value, or null if not found.
     */
    public static String getStringParameter(String name, Map<String, List<SmooksResourceConfiguration>> resourceConfigurations) {
        Parameter parameter = getParameter(name, resourceConfigurations);

        if(parameter != null) {
            return parameter.getValue();
        }

        return null;
    }

    public static void setParameter(String name, String value, Smooks smooks) {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration(ParameterAccessor.GLOBAL_PARAMETERS);

        config.setParameter(name, value);
        SmooksUtil.registerResource(config, smooks);
    }

    public static void removeParameter(String name, Smooks smooks) {
    	SmooksResourceConfigurationStore configStore = smooks.getApplicationContext().getStore();
    	Iterator<SmooksResourceConfigurationList> configLists = configStore.getSmooksResourceConfigurationLists();

    	while(configLists.hasNext()) {
            SmooksResourceConfigurationList list = configLists.next();
            for(int i = 0; i < list.size(); i++) {
                SmooksResourceConfiguration nextConfig = list.get(i);
                if(ParameterAccessor.GLOBAL_PARAMETERS.equals(nextConfig.getSelector())) {
                	nextConfig.removeParameter(name);
                }
            }
        }
    }
}
