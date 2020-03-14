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

package org.smooks.edi;

import org.junit.Test;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.dom.DOMParser;
import org.smooks.edisax.model.EdifactModel;
import org.smooks.io.StreamUtils;
import org.smooks.payload.StringResult;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Tests for SmooksEDIParser.
 * @author tfennelly
 */
public class SmooksEDIParserTest {

	private static final String TEST_XML_MAPPING_XML_URI = "classpath:/org/smooks/edi/edi-to-xml-mapping.xml";

    @Test
    public void test_cyclic_dependency() throws IOException, SAXException {
		String mapping = new String(StreamUtils.readStream(getClass().getResourceAsStream("cyclicDependencyTest/edi-to-xml-mapping.xml")));
		test_cyclic_dependency(mapping);
	}

    @Test
    public void test_import_resource() throws IOException, SAXException {
		String mapping = new String(StreamUtils.readStream(getClass().getResourceAsStream("definitionTest/edi-to-xml-mapping.xml")));
		test_import(mapping);
	}

    @Test
    public void test_inlined() throws IOException, SAXException {
		String mapping = new String(StreamUtils.readStream(getClass().getResourceAsStream("edi-to-xml-mapping.xml")));
		test(mapping);
	}

        @Test
	public void test_uri_based() throws IOException, SAXException {
		test(TEST_XML_MAPPING_XML_URI);
	}

        @Test
	public void test_invalid_config() throws IOException, SAXException {
		// Mandatory "mapping-model" config param not specified...
		try {
			test(null);
			fail("Expected SmooksConfigurationException.");
		} catch(IllegalArgumentException e) {
			assertEquals("null or empty 'mappingModel' arg in method call.", e.getMessage());
		}

		// Mandatory "mapping-model" config param is a valid URI, but doesn't point at anything that exists...
		try {
			test("http://nothing/there.xml");
			fail("Expected IllegalStateException.");
		} catch(SmooksException e) {
			assertEquals("Invalid EDI mapping model config specified for org.smooks.edisax.EDIParser.  Unable to access URI based mapping model [http://nothing/there.xml].", e.getCause().getMessage());
		}

		// Mandatory "mapping-model" config param is not a valid URI, nor is it a valid inlined config...
		try {
			test("<z/>");
			fail("Expected SAXException.");
		} catch(SmooksException e) {
			assertEquals("Error parsing EDI Mapping Model [<z/>].", e.getCause().getMessage());
		}
	}

        @Test
	public void test_caching() throws IOException, SAXException {
		byte[] input = StreamUtils.readStream(getClass().getResourceAsStream("edi-input.txt"));
		Smooks smooks = new Smooks();
		SmooksResourceConfiguration config = null;

		// Create and initialise the Smooks config for the parser...
		config = new SmooksResourceConfiguration();
        config.setResource(EDIReader.class.getName());
		// Set the mapping config on the resource config...
        config.setParameter(EDIReader.MODEL_CONFIG_KEY, TEST_XML_MAPPING_XML_URI);

		DOMParser parser;

		// Create 1st parser using the config, and run a parse through it...
		parser = new DOMParser(smooks.createExecutionContext(), config);
		parser.parse(new StreamSource(new ByteArrayInputStream(input)));

		// Check make sure the parsed and validated model was cached...
		Hashtable mappingTable = EDIReader.getMappingTable(smooks.getApplicationContext());
		assertNotNull("No mapping table in context!", mappingTable);

        EdifactModel mappingModel_request1 = (EdifactModel) mappingTable.get(config);
        assertNotNull("No mapping model in mapping table!", mappingModel_request1);

		// Create 2nd parser using the same config, and run a parse through it...
		parser = new DOMParser(smooks.createExecutionContext(), config);
		parser.parse(new StreamSource(new ByteArrayInputStream(input)));

		// Make sure the cached model was used on the 2nd parse...
		assertEquals("Not the same model instance => cache not working properly!", mappingModel_request1, (EdifactModel) mappingTable.get(config));
	}

    private void test_import(String mapping) throws IOException, SAXException {
		InputStream input = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("definitionTest/edi-input.txt")));
		String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("definitionTest/expected.xml")));
		Smooks smooks = new Smooks();
		SmooksResourceConfiguration config = null;

		// Create and initialise the Smooks config for the parser...
        config = new SmooksResourceConfiguration();
        config.setResource(EDIReader.class.getName());
		// Set the mapping config on the resource config...
		if(mapping != null) {
			config.setParameter(EDIReader.MODEL_CONFIG_KEY, mapping);
		}

		DOMParser parser = new DOMParser(smooks.createExecutionContext(), config);
		Document doc = parser.parse(new StreamSource(input));

		Diff diff = new Diff(expected, XmlUtil.serialize(doc.getChildNodes()));
		assertTrue(diff.toString(), diff.identical());
	}

    private void test_cyclic_dependency(String mapping) throws IOException, SAXException {
		InputStream input = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("cyclicDependencyTest/edi-input.txt")));

		Smooks smooks = new Smooks();
		SmooksResourceConfiguration config = null;

		// Create and initialise the Smooks config for the parser...
        config = new SmooksResourceConfiguration();
        config.setResource(EDIReader.class.getName());
		// Set the mapping config on the resource config...
		if(mapping != null) {
			config.setParameter(EDIReader.MODEL_CONFIG_KEY, mapping);
		}

		DOMParser parser = new DOMParser(smooks.createExecutionContext(), config);
        try {
            parser.parse(new StreamSource(input));
            assert false : "Parser should fail when importing importing message mappings with cyclic dependency";
        } catch (Exception e) {
            assert true : "Parser should fail when importing importing message mappings with cyclic dependency";
        }

    }

    private void test(String mapping) throws IOException, SAXException {
		String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("expected.xml")));
        Smooks smooks = new Smooks();
        StringResult result = new StringResult();

        // Create and initialise the Smooks config for the parser...
        smooks.setReaderConfig(new EDIReaderConfigurator(mapping));
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("edi-input.txt")), result);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(expected, result.toString());
	}
}
