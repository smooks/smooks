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

package org.milyn.json;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.payload.StringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class JSONReaderExtendedConfigTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONReaderExtendedConfigTest.class);

    @Test
    public void test_simple_smooks_config() throws Exception {
    	test_config_file("simple_smooks_config");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new JSONReaderConfigurator());
        test_config_file("simple_smooks_config", smooks);
    }

    @Test
    public void test_key_replacement() throws Exception {
    	test_config_file("key_replacement");

        // Programmatic config....
        Smooks smooks = new Smooks();

        Map<String, String> keyMap = new HashMap<String, String>();

        keyMap.put("some key", "someKey");
        keyMap.put("some&key", "someAndKey");

        smooks.setReaderConfig(new JSONReaderConfigurator().setKeyMap(keyMap));
        test_config_file("key_replacement", smooks);
    }

    @Test
    public void test_several_replacements() throws Exception {
    	test_config_file("several_replacements");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new JSONReaderConfigurator()
                .setKeyWhitspaceReplacement("_")
                .setKeyPrefixOnNumeric("n")
                .setIllegalElementNameCharReplacement(".")
                .setNullValueReplacement("##NULL##"));

        test_config_file("several_replacements", smooks);
    }

    @Test
    public void test_configured_different_node_names() throws Exception {
    	test_config_file("configured_different_node_names");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new JSONReaderConfigurator()
                .setRootName("root")
                .setArrayElementName("e"));
        test_config_file("configured_different_node_names", smooks);
    }

    @Test
    public void test_indent() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("indent-config.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("input-message.jsn")), result);

        assertTrue(XMLUnit.compareXML(StreamUtils.readStreamAsString(getClass().getResourceAsStream("indent-expected.xml")), result.toString()).identical());
    }

    private void test_config_file(String testName) throws Exception {
        Smooks smooks = new Smooks("/test/" + testName + "/smooks-extended-config.xml");

        test_config_file(testName, smooks);
    }

    private void test_config_file(String testName, Smooks smooks) throws IOException {
        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testName + "/input-message.jsn"), smooks);

        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Result: " + result);
        }

        assertEquals("/test/" + testName + "/expected.xml", result.getBytes());
    }

    private void assertEquals(String fileExpected, byte[] actual) throws IOException {

		byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream(fileExpected));

        assertTrue("Expected XML and result XML are not the same!", StreamUtils.compareCharStreams(new ByteArrayInputStream(actual), new ByteArrayInputStream(expected)));

	}
}
