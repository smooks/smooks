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

package org.milyn.xml;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * Smooks {@link XMLReader}.
 * <p/>
 * {@link org.milyn.delivery.dom.DOMParser} allows you to target a specific SAX Parser at a specific message type.
 * This lets you parse a stream of any type, convert it to a stream of SAX event and so treat the stream
 * as an XML data stream, even when the stream is non-XML.
 * <p/>
 * The parser resource configuration "selector" needs to have a value of "org.xml.sax.driver" i.e.
 * <b>selector="org.xml.sax.driver"</b>.
 *
 * @author tfennelly
 */
public interface SmooksXMLReader extends XMLReader, ContentHandler {

	/**
	 * Set the Smooks {@link ExecutionContext} on the implementing class.
	 * @param executionContext The Smooks {@link ExecutionContext}.
	 */
	public void setExecutionContext(ExecutionContext executionContext);
}
