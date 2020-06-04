/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.servlet;

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
