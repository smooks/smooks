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
package org.milyn.edit.utils.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jdom.Document;
import org.jdom.input.SAXHandler;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.milyn.edi.utils.EDIParsingUtils;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Test for parsing D99A file
 * 
 **/
public class D99AParsingTest {

        @Test
	public void test99A_cuscar() throws Exception {
		InputStream inputStream = D99AParsingTest.class.getResourceAsStream("/99a_cuscar.edi");
		parseAndValidate(inputStream);
	}

        @Test
	public void test96B_cusdec() throws Exception {
		InputStream inputStream = D99AParsingTest.class.getResourceAsStream("/96b_cusdec.edi");
		parseAndValidate(inputStream);
	}

	
	private void parseAndValidate(InputStream inputStream) throws IOException,
			SAXException, SAXNotRecognizedException, SAXNotSupportedException {
		assertNotNull(inputStream);
		UNEdifactInterchangeParser parser = new UNEdifactInterchangeParser();
		parser.setFeature(EDIParser.FEATURE_IGNORE_NEWLINES, true);
		SAXHandler handler = new SAXHandler();
		parser.setContentHandler(handler);
		parser.parse(new InputSource(inputStream));
		Document doc = handler.getDocument();
		// Here you have your document
		StringWriter xmlWriter = new StringWriter();
		new XMLOutputter(Format.getPrettyFormat()).output(doc, xmlWriter);
		String xml = xmlWriter.getBuffer().toString();
		XMLReader reader = EDIParsingUtils.createValidatingReader();
		ExtensiveErrorHandler errorHandler = new ExtensiveErrorHandler(xml);
		reader.setErrorHandler(errorHandler);
		reader.parse(new InputSource(new StringReader(xml)));
		assertFalse("There were validation errors or warnings", errorHandler.hasErrors());
	}
	
}
