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
package org.smooks;

import org.smooks.cdr.SmooksResourceConfiguration;

import java.util.List;

/**
 * Reader configurator.
 * <p/>
 * Implementation are responsible creating the {@link org.smooks.cdr.SmooksResourceConfiguration} for
 * the Reader to be used by a Smooks instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public interface ReaderConfigurator {

    /**
     * Create the {@link org.smooks.cdr.SmooksResourceConfiguration} list for the Reader to be used by the Smooks instance.
     * @return The {@link org.smooks.cdr.SmooksResourceConfiguration} list.
     */
    List<SmooksResourceConfiguration> toConfig();
}
