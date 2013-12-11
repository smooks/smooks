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
package org.milyn.delivery.java;

import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.xml.SmooksXMLReader;

import java.util.List;

/**
 * Java {@link org.xml.sax.XMLReader} for Smooks.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface JavaXMLReader extends SmooksXMLReader {

    /**
     * Set the list of Source Java objects to be used to generate events on the
     * {@link org.xml.sax.ContentHandler} set on this {@link org.xml.sax.XMLReader}.
     *
     * @param sourceObjects Java source Object list.
     * @throws SmooksConfigurationException Unable to set source Java Objects.
     */
    public void setSourceObjects(List<Object> sourceObjects) throws SmooksConfigurationException;
}
