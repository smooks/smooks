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

package org.milyn.servlet;

import javax.servlet.ServletConfig;

/**
 * Servlet parameter utilities.
 * 
 * @author tfennelly
 */
public abstract class ServletParamUtils {

	/**
	 * Get a Servlet configuration parameter. <p/> This method will check the
	 * Servlet configuration (init-param config parameters) for a definition of
	 * 'paramName'. If not specified in the Servlet configuration, it will check
	 * the servlet context configuration parameters (context-param).
	 * 
	 * @param paramName
	 *            The name of the parameter.
	 * @param config
	 *            The ServletConfig instance.
	 * @return The parameter value, or null if not defined.
	 */
	public static String getParameterValue(String paramName,
			ServletConfig config) {
		String configParam = null;

		if (paramName == null) {
			throw new IllegalArgumentException(
					"null 'paramName' paramater in method call.");
		} else if (paramName.trim().equals("")) {
			throw new IllegalArgumentException(
					"empty 'paramName' paramater in method call.");
		} else if (config == null) {
			throw new IllegalArgumentException(
					"null 'config' paramater in method call.");
		}

		// Try the Servlet config.
		configParam = config.getInitParameter(paramName);
		if (configParam == null) {
			// Try the Servlet Context config.
			configParam = config.getServletContext()
					.getInitParameter(paramName);
		}

		return configParam;
	}

	/**
	 * Get a Servlet configuration parameter. <p/> This method will check the
	 * Servlet configuration (init-param config parameters) for a definition of
	 * 'paramName'. If not specified in the Servlet configuration, it will check
	 * the servlet context configuration parameters (context-param).
	 * 
	 * @param paramName
	 *            The name of the parameter.
	 * @param config
	 *            The ServletConfig instance.
	 * @param defaultVal
	 *            The value returned if the pararameter is not defined.
	 * @return The parameter value, or 'defaultVal' if not defined.
	 */
	public static String getParameterValue(String paramName,
			ServletConfig config, String defaultVal) {
		String value = getParameterValue(paramName, config);
		return (value != null ? value : defaultVal);
	}
}
