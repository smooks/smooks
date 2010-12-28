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

package org.milyn.cdr;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.assertion.AssertArgument;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;

/**
 * Accessor class for looking up global parameters.
 * <p id="decode"/>
 * Profile specific parameters are stored under the "global-parameters" selector
 * (see {@link org.milyn.cdr.SmooksResourceConfiguration}).  The parameter values are
 * stored in the &lt;param&gt; elements within this Content Delivery Resource definition.
 * This class iterates over the list of {@link org.milyn.cdr.SmooksResourceConfiguration} 
 * elements targeted at the {@link org.milyn.container.ExecutionContext} profile.  It looks for a definition of the named
 * parameter.  If the &lt;param&gt; has a type attribute the 
 * {@link org.milyn.cdr.ParameterDecoder} for that type can be applied to the attribute
 * value through the {@link #getParameterObject(String,org.milyn.delivery.ContentDeliveryConfig)} method,
 * returning whatever Java type defined by the {@link org.milyn.cdr.ParameterDecoder}
 * implementation.  As an example, see {@link org.milyn.cdr.TokenizedStringParameterDecoder}.
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
     * Calls {@link org.milyn.delivery.ContentDeliveryConfig#getSmooksResourceConfigurations()}
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
