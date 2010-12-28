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

package org.milyn.css;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;

import junit.framework.TestCase;

import org.milyn.container.MockContainerResourceLocator;
import org.milyn.magger.CSSParser;
import org.milyn.magger.CSSStylesheet;
import org.milyn.xml.XmlUtil;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;

public abstract class CssTestUtil {

	public static CSSStylesheet parseCSS(String classpath) {
		org.w3c.css.sac.InputSource inputSrc = new InputSource(new InputStreamReader(CssTestUtil.class.getResourceAsStream(classpath)));
		URI baseURI = URI.create("http://milyn.codehaus.org");
		CSSParser parser = new CSSParser(new MockContainerResourceLocator());
		CSSStylesheet styleSheet = null;

		try {
			styleSheet = parser.parse(inputSrc, baseURI, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			TestCase.fail(e.getMessage());
		}
		return styleSheet;
	}

	public static Document parseXMLString(String xmlString) {
		try {
			return XmlUtil.parseStream(new ByteArrayInputStream(xmlString.getBytes()), XmlUtil.VALIDATION_TYPE.NONE, true);
		} catch (Exception e) {
			e.printStackTrace();
			TestCase.fail(e.getMessage());
		}
		return null;
	}

	public static Document parseCPResource(String classpath) {
		try {
			return XmlUtil.parseStream(CssTestUtil.class.getResourceAsStream(classpath), XmlUtil.VALIDATION_TYPE.NONE, true);
		} catch (Exception e) {
			e.printStackTrace();
			TestCase.fail(e.getMessage());
		}
		return null;
	}
}