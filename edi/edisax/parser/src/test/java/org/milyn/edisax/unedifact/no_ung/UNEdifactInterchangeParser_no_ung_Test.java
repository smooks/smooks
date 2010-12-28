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
package org.milyn.edisax.unedifact.no_ung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.MockContentHandler;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;
import org.milyn.io.StreamUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactInterchangeParser_no_ung_Test extends TestCase {

	public void test_unzipped() throws IOException, SAXException, EDIConfigurationException {
		EdifactModel model1 = EDIParser.parseMappingModel(getClass().getResourceAsStream("../MSG1-model.xml"));
		EdifactModel model2 = EDIParser.parseMappingModel(getClass().getResourceAsStream("../MSG2-model.xml"));
		
		UNEdifactInterchangeParser parser = new UNEdifactInterchangeParser();
		parser.addMappingModel(model1).addMappingModel(model2);

		testExchanges(parser);
	}

	public void test_zipped() throws IOException, SAXException, EDIConfigurationException {
		createZip();

		UNEdifactInterchangeParser parser = new UNEdifactInterchangeParser();
		parser.addMappingModels("mapping-models.zip", URI.create("./target"));

		testExchanges(parser);
	}

	private void testExchanges(UNEdifactInterchangeParser parser) throws IOException, SAXException {		
		MockContentHandler handler;
		
		// Test message 01 - no UNA segment...
		handler = new MockContentHandler();
		parser.setContentHandler(handler);		
		parser.parse(new InputSource(getClass().getResourceAsStream("unedifact-msg-01.edi")));		
		//System.out.println(handler.xmlMapping);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected.xml")), new StringReader(handler.xmlMapping.toString()));

		// Test message 01 - has a UNA segment...
		handler = new MockContentHandler();
		parser.setContentHandler(handler);		
		parser.parse(new InputSource(getClass().getResourceAsStream("unedifact-msg-02.edi")));
		
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected.xml")), new StringReader(handler.xmlMapping.toString()));
	}

	private void createZip() throws IOException {
		File zipFile = new File("target/mapping-models.zip");
		
		zipFile.delete();
		
		ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));		
		try {
			addZipEntry("test/models/subs/MSG1-model.xml", "../MSG1-model.xml", zipStream);
			addZipEntry("test/models/subs/MSG2-model.xml", "../MSG2-model.xml", zipStream);
			addZipEntry("test/models/MSG3-model.xml", "../MSG3-model.xml", zipStream);
			addZipEntry(EDIUtils.EDI_MAPPING_MODEL_ZIP_LIST_FILE, "../mapping-models.lst", zipStream);
		} finally {
			zipStream.close();
		}
	}

	private void addZipEntry(String name, String resource, ZipOutputStream zipStream) throws IOException {
		ZipEntry zipEntry = new ZipEntry(name);
		byte[] resourceBytes = StreamUtils.readStream(getClass().getResourceAsStream(resource));
		
		zipStream.putNextEntry(zipEntry);
		zipStream.write(resourceBytes);
	}
}
