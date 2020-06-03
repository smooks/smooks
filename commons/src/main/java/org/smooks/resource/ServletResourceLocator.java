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
package org.smooks.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.servlet.ServletConfig;

import org.smooks.servlet.ServletParamUtils;

/**
 * Servlet resource locator.
 * 
 * @author tfennelly
 */
public class ServletResourceLocator implements ContainerResourceLocator {

	/**
	 * Servlet configuration instance.
	 */
	private ServletConfig config = null;

	/**
	 * Locator for external resource.
	 */
	private ExternalResourceLocator externalResourceLocator = null;

	/**
	 * Public constructor.
	 * 
	 * @param config
	 *            The ServletConfig instance.
	 * @param externalResourceLocator
	 *            Resource locator for resources outside the Servlet context.
	 */
	public ServletResourceLocator(ServletConfig config,
			ExternalResourceLocator externalResourceLocator) {
		if (config == null) {
			throw new IllegalArgumentException(
					"null 'config' paramater in method call.");
		}
		this.config = config;
		this.externalResourceLocator = externalResourceLocator;
	}

	/**
	 * Get a resource stream through the Servlet container. <p/> If a Servlet
	 * configuration (see
	 * {@link ServletParamUtils#getParameterValue(String, ServletConfig)})
	 * exists for "configName" the method uses use the config value, otherwise
	 * it use the "defaultUri" value provided. Calls
	 * {@link #getResource(String)} to get the resource stream.
	 */
	public InputStream getResource(String configName, String defaultUri)
			throws IllegalArgumentException, IOException {
		String configVal;

		if (defaultUri != null && defaultUri.trim().equals("")) {
			throw new IllegalArgumentException(
					"empty 'defaultUri' paramater in method call.");
		}
		configVal = ServletParamUtils.getParameterValue(configName, config);

		if (configVal == null) {
			// Default the load location.
			if (defaultUri != null) {
				config.getServletContext().log(
						"[Milyn] Defaulting resource [" + configName
								+ "] load location to: " + defaultUri);
				configVal = defaultUri;
			} else {
				throw new IllegalArgumentException(
						"Resource ["
								+ configName
								+ "] not specified in configuration, plus no default load location provided.");
			}
		} else {
			config.getServletContext().log(
					"[Milyn] Resource [" + configName
							+ "] load location[servlet-context]: " + configVal);
		}

		InputStream resourceStream = getResource(configVal);
		if (resourceStream == null) {
			if (configVal == defaultUri) {
				throw new IOException("Unable to access default [" + configName
						+ "] resource: " + defaultUri);
			} else {
				throw new IllegalArgumentException(
						"Invalid resource parameter ["
								+ configName
								+ "="
								+ configVal
								+ "] defined in deployment descriptor.  Unable to access specified resource.");
			}
		}

		return resourceStream;
	}

	/**
	 * Get a resource stream through the Servlet container. <p/> Loads the
	 * resource through the first successfull call of:
	 * <ul>
	 * <li>{@link javax.servlet.ServletContext#getResourceAsStream(java.lang.String)}</li>
	 * <li>{@link ExternalResourceLocator#getResource(String)}</li>
	 * </ul>
	 */
	public InputStream getResource(String uri) throws IllegalArgumentException,
			IOException {
		InputStream resourceStream = config.getServletContext()
				.getResourceAsStream(uri);

		if (resourceStream == null) {
			// Might be a config resource in which case it might be in the
			// WEB-INF folder.
			resourceStream = config.getServletContext().getResourceAsStream(
					"/WEB-INF" + uri);
		}

		// If it's not context relative it must be absolute => external.
		if (resourceStream == null && externalResourceLocator != null) {
			resourceStream = externalResourceLocator.getResource(uri);
		}

		return resourceStream;
	}

	public URI getBaseURI() {
		return URI.create(config.getServletContext().getContextPath());
	}
}
