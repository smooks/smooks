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

package org.milyn.delivery;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.cdr.SmooksConfigurationException;


/**
 * ContentHandler factory interface.
 *
 * @author tfennelly
 */
public interface ContentHandlerFactory extends ContentHandler {

    /**
     * Name of the param used on a ContentHandlerFactory config that specifies
     * the resource type that the creator is adding support for.  This is different
     * from the type attribute on the resource element.  In the case of a ContentHandlerFactory
     * configuration, the ContentHandlerFactory impl resource type is "class", but it's adding
     * support for something else (e.g. "xsl").  This is why we can't use the type attribute for this
     * purpose.
     */
    public static final String PARAM_RESTYPE = "restype";

    /**
     * Create the content handler instance.
     *
     * @param resourceConfig The SmooksResourceConfiguration for the {@link ContentHandler}
     *                       to be created.
     * @return Content handler instance.
     * @throws SmooksConfigurationException Successfully created ContentHandler, but an error occured during configuration.
     * @throws InstantiationException       Unable to create ContentHandler instance.
     */
    public Object create(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException, InstantiationException;
}
