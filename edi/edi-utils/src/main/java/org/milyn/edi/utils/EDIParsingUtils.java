/*
 * Milyn - Copyright (C) 2006 - 2011
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
package org.milyn.edi.utils;

import java.io.IOException;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.XML11Configuration;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Utility class with helpers to construct validating parser of the EDIFACT
 * files
 * 
 * @author zubairov
 * 
 */
public class EDIParsingUtils {

	/**
	 * This utility method creates an {@link XMLReader} that will validate XML
	 * moreover it will load XML Schemas from the Smooks mapping JAR files
	 * presented in the classpath.
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXNotRecognizedException
	 * @throws SAXNotSupportedException
	 */
	public static XMLReader createValidatingReader() throws IOException, SAXNotRecognizedException, SAXNotSupportedException {
		String validationFeature = "http://xml.org/sax/features/validation";
		String schemaFeature = "http://apache.org/xml/features/validation/schema";
		XML11Configuration config = new XML11Configuration();
		config.setEntityResolver(EDISchemaEntityManager.createInstance());
		XMLReader result = new SAXParser(config);
		result.setFeature(validationFeature, true);
		result.setFeature(schemaFeature, true);
		return result;
	}
	
	

}
