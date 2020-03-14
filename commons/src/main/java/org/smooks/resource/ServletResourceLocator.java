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
