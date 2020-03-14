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

package org.smooks.yaml;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.SmooksUtil;
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class YamlReaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(YamlReaderTest.class);

        @Test
	public void test_yaml_types() throws Exception {

        testBasic("yaml_types");
	}

        @Test
	public void test_yaml_map() throws Exception {

        testBasic("yaml_map");
	}

        @Test
	public void test_yaml_array() throws Exception {

        testBasic("yaml_array");
	}

        @Test
	public void test_yaml_map_array() throws Exception {

        testBasic("yaml_map_array");
	}

        @Test
	public void test_yaml_array_map() throws Exception {

        testBasic("yaml_array_map");
	}

        @Test
	public void test_yaml_map_array_map() throws Exception {
        testBasic("yaml_map_array_map");
	}


    @Test
    public void test_yaml_multi_documents() throws Exception {
    	testBasic("yaml_multi_documents");
    }

    @Test
    public void test_simple_smooks_config() throws Exception {
    	testCoreConfigFile("simple_smooks_config");

    	testExtendedConfigFile("simple_smooks_config");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator());
        testProgrammaticConfig("simple_smooks_config", smooks);
    }

    @Test
    public void test_key_replacement() throws Exception {
    	testCoreConfigFile("key_replacement");

    	testExtendedConfigFile("key_replacement");

        // Programmatic config....
        Smooks smooks = new Smooks();

        Map<String, String> keyMap = new HashMap<String, String>();

        keyMap.put("some key", "someKey");
        keyMap.put("some&key", "someAndKey");

        smooks.setReaderConfig(new YamlReaderConfigurator().setKeyMap(keyMap));
        testProgrammaticConfig("key_replacement", smooks);
    }

    @Test
    public void test_several_replacements() throws Exception {
    	testCoreConfigFile("several_replacements");

    	testExtendedConfigFile("several_replacements");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new YamlReaderConfigurator()
                .setKeyWhitspaceReplacement("_")
                .setKeyPrefixOnNumeric("n")
                .setIllegalElementNameCharReplacement("-"));

        testProgrammaticConfig("several_replacements", smooks);
    }

    @Test
    public void test_configured_different_node_names() throws Exception {
    	testCoreConfigFile("configured_different_node_names");

    	testExtendedConfigFile("configured_different_node_names");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new YamlReaderConfigurator()
                .setRootName("root")
                .setDocumentName("doc")
                .setArrayElementName("e"));
        testProgrammaticConfig("configured_different_node_names", smooks);
    }

    @Test
    public void test_indent() throws Exception {
    	testCoreConfigFile("indent");
    	testExtendedConfigFile("indent");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new YamlReaderConfigurator().setIndent(true));
        testProgrammaticConfig("indent", smooks);
    }

    @Test
    public void test_alias_with_refer() throws Exception {
    	testCoreConfigFile("alias_with_refer");
    	testExtendedConfigFile("alias_with_refer");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator());
        testProgrammaticConfig("alias_with_refer", smooks);
    }

    @Test
    public void test_alias_with_refer_different_attribute_names() throws Exception {
    	testCoreConfigFile("alias_with_refer_different_attribute_names");
    	testExtendedConfigFile("alias_with_refer_different_attribute_names");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator()
        							.setAnchorAttributeName("anchor")
        							.setAliasAttributeName("alias"));
        testProgrammaticConfig("alias_with_refer_different_attribute_names", smooks);
    }

    @Test
    public void test_alias_with_resolve() throws Exception {
    	testCoreConfigFile("alias_with_resolve");
    	testExtendedConfigFile("alias_with_resolve");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator().setAliasStrategy(AliasStrategy.RESOLVE));
        testProgrammaticConfig("alias_with_resolve", smooks);
    }

    @Test
    public void test_alias_with_resolve_without_anchor() throws Exception {
    	try {
	        // Programmatic config....
	        Smooks smooks = new Smooks();
	        smooks.setReaderConfig(new YamlReaderConfigurator().setAliasStrategy(AliasStrategy.RESOLVE));
	        testProgrammaticConfig("alias_with_resolve_without_anchor", smooks);
    	} catch (SmooksException e) {

    		Throwable cause = e.getCause();

    		assertArrayEquals("A non existing anchor with the name 'id1' is referenced by the alias of the element 'keyWithAlias'. The anchor must be declared before it can be referenced by an alias.".toCharArray(), cause.getMessage().toCharArray());

    		return;
		}
    	fail("Expected exception was not thrown!");
    }

    @Test
    public void test_alias_with_resolve_with_anchor_after_alias() throws Exception {
    	try {
	        // Programmatic config....
	        Smooks smooks = new Smooks();
	        smooks.setReaderConfig(new YamlReaderConfigurator().setAliasStrategy(AliasStrategy.RESOLVE));
	        testProgrammaticConfig("alias_with_resolve_with_anchor_after_alias", smooks);
    	} catch (SmooksException e) {

    		Throwable cause = e.getCause();

    		assertArrayEquals("A non existing anchor with the name 'id1' is referenced by the alias of the element 'keyWithAlias'. The anchor must be declared before it can be referenced by an alias.".toCharArray(), cause.getMessage().toCharArray());

    		return;
		}
    	fail("Expected exception was not thrown!");
    }

    @Test
    public void test_alias_with_resolve_with_anchor_as_parent() throws Exception {
    	try {
	        // Programmatic config....
	        Smooks smooks = new Smooks();
	        smooks.setReaderConfig(new YamlReaderConfigurator().setAliasStrategy(AliasStrategy.RESOLVE));
	        testProgrammaticConfig("alias_with_resolve_with_anchor_as_parent", smooks);
    	} catch (SmooksException e) {

    		Throwable cause = e.getCause();

    		assertArrayEquals("The alias to anchor 'id1' is declared within the element structure in which on of the parent elements declares the anchor. This is not allowed because it leads to infinite loops.".toCharArray(), cause.getMessage().toCharArray());

    		return;
		}
    	fail("Expected exception was not thrown!");
    }

    @Test
    public void test_alias_with_refer_resolve() throws Exception {
    	testCoreConfigFile("alias_with_refer_resolve");
    	testExtendedConfigFile("alias_with_refer_resolve");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator().setAliasStrategy(AliasStrategy.REFER_RESOLVE));
        testProgrammaticConfig("alias_with_refer_resolve", smooks);
    }

    @Test
    public void test_alias_with_refer_resolve_different_attribute_names() throws Exception {
    	testCoreConfigFile("alias_with_refer_resolve_different_attribute_names");
    	testExtendedConfigFile("alias_with_refer_resolve_different_attribute_names");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new YamlReaderConfigurator()
        							.setAliasStrategy(AliasStrategy.REFER_RESOLVE)
        							.setAnchorAttributeName("anchor")
        							.setAliasAttributeName("alias"));
        testProgrammaticConfig("alias_with_refer_resolve_different_attribute_names", smooks);
    }


	private void testBasic(String testName) throws Exception{
		Smooks smooks = new Smooks();

        smooks.setReaderConfig(new YamlReaderConfigurator().setIndent(true));

        testProgrammaticConfig(testName, smooks);
	}

    private void testCoreConfigFile(String testName) throws Exception {
        Smooks smooks = new Smooks("/test/" + testName + "/smooks-config.xml");

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testName + "/input-message.yaml"), smooks);

        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Result: " + result);
        }

        assertEquals("/test/" + testName + "/expected.xml", result.getBytes());
    }


    private void testExtendedConfigFile(String testName) throws Exception {
        Smooks smooks = new Smooks("/test/" + testName + "/smooks-extended-config.xml");

        testProgrammaticConfig(testName, smooks);
    }

    private void testProgrammaticConfig(String testName, Smooks smooks) throws IOException, SAXException {
        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testName + "/input-message.yaml"), smooks);

        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Result: " + result);
        }

        assertEquals("/test/" + testName + "/expected.xml", result.getBytes());
    }

	private void assertEquals(String fileExpected, byte[] actual) throws IOException, SAXException {

		String expected = StreamUtils.readStreamAsString(getClass().getResourceAsStream(fileExpected));

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(expected, new String(actual));
	}
}
