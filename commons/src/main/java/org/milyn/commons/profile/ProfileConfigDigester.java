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

package org.milyn.commons.profile;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

/**
 * Stream based profile configuration digester interface. <p/> Construct a
 * ProfileStore from an input stream. Most likely this stream would be of an XML
 * format but this is implementation dependent - could be a serialised
 * ProfileStore stream with the digester performing the deserialisation. <p/>
 * Implementations must provide a default/empty constructor.
 * 
 * @author tfennelly
 */
public interface ProfileConfigDigester {

	/**
	 * Parse the device profile configuration stream.
	 * 
	 * @param input
	 *            The input stream instance.
	 * @return ProfileStore instance.
	 */
	public abstract ProfileStore parse(InputStream input) throws SAXException,
			IOException;
}