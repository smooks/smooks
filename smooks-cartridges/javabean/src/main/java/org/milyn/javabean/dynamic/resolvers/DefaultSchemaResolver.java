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
package org.milyn.javabean.dynamic.resolvers;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default Namespace XML Schema resolver.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultSchemaResolver extends AbstractResolver {
	
	public DefaultSchemaResolver(List<Properties> descriptors) {
		super(descriptors);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		return resolveSchemaLocation(systemId);
	}	
}