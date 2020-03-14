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
package org.smooks.delivery;

import org.smooks.cdr.SmooksResourceConfiguration;

import java.util.List;

/**
 * Interface to allow configuration expansion.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ConfigurationExpander extends ContentHandler {

    /**
     * Get the additional configurations to be added to the delivery config by
     * this ContentHandler.
     *
     * @return A list of expansion configurations, or an empty list if no configutations
     *         are to be added for this instance.
     */
    public List<SmooksResourceConfiguration> expandConfigurations();
}
