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

package org.milyn.delivery;

import org.milyn.container.ExecutionContext;
import org.milyn.xml.hierarchy.HierarchyChangeListener;
import org.xml.sax.XMLReader;

/**
 * ExecutionContext aware XMLReader change listener.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XMLReaderHierarchyChangeListener implements HierarchyChangeListener {

    private ExecutionContext executionContext;

    public XMLReaderHierarchyChangeListener(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void attachXMLReader(XMLReader xmlReader) {
        AbstractParser.attachXMLReader(xmlReader, executionContext);
    }

    public void detachXMLReader() {
        AbstractParser.detachXMLReader(executionContext);
    }
}
