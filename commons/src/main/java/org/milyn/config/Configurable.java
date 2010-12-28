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
package org.milyn.config;

import org.milyn.cdr.SmooksConfigurationException;

import java.util.Properties;

/**
 * Configurable component.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public interface Configurable {

    /**
     * Set the component configuration.
     * @param config The component configuration properties.
     * @throws SmooksConfigurationException Bad component configuration.
     */
    void setConfiguration(Properties config) throws SmooksConfigurationException;

    /**
     * Get the component configuration.
     * @return The component configuration properties.
     */
    Properties getConfiguration();
}
