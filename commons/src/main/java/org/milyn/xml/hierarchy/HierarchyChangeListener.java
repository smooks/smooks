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

package org.milyn.xml.hierarchy;

import org.xml.sax.XMLReader;

/**
 * Hierarchy change listener.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface HierarchyChangeListener {

    /**
     * Attach a new reader to the reader hierarchy.
     *
     * @param xmlReader The XML reader that has been attached.
     */
    void attachXMLReader(XMLReader xmlReader);

    /**
     * Detach the last attached reader.
     */
    void detachXMLReader();
}
