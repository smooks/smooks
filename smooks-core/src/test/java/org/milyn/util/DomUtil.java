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

package org.milyn.util;

import junit.framework.TestCase;
import org.milyn.commons.xml.XmlUtil;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public abstract class DomUtil {


    /**
     * Parse a stream directly, handling all exceptions by failing the testcase.
     *
     * @param stream Stream to be parsed.
     * @return W3C DOM.
     */
    public static Document parse(String xmlString) {
        return DomUtil.parse(new ByteArrayInputStream(xmlString.getBytes()));
    }

    /**
     * Parse a stream directly, handling all exceptions by failing the testcase.
     *
     * @param stream Stream to be parsed.
     * @return W3C DOM.
     */
    public static Document parse(InputStream stream) {
        try {
            return XmlUtil.parseStream(stream, XmlUtil.VALIDATION_TYPE.NONE, true);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail("Failed to parse Document stream: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parse the specified classpath resource, handling all exceptions by failing the testcase.
     *
     * @param classpathLoc  Classpath resource to be parsed.
     * @param relativeClass Class instance used to get the resource stream.
     * @return W3C DOM.
     */
    public static Document parse(String classpathLoc, Class relativeClass) {
        return DomUtil.parse(relativeClass.getResourceAsStream(classpathLoc));
    }
}
