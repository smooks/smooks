/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.edisax.interchange;

import org.xml.sax.SAXException;

/**
 * Interchange control block handler factory.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ControlBlockHandlerFactory {

    String ENVELOPE_PREFIX = "env";
    String NAMESPACE_ROOT = "urn:org.milyn.edi.unedifact";

    /**
     * Get the transmission namespace.
     * @return The transmission namespace.
     */
    String getNamespace();

    /**
     * Get a {@link ControlBlockHandler} instance for the specified interchange segment code.
     * @param segCode The segment code.
     * @return The interchange control block handler.
     * @throws SAXException Unknown control block segment code.
     */
	ControlBlockHandler getControlBlockHandler(String segCode) throws SAXException;
}