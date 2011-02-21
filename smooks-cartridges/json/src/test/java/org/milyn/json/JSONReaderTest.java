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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.profile.DefaultProfileSet;

/**
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class JSONReaderTest extends TestCase {

	private static final Log logger = LogFactory.getLog(JSONReaderTest.class);

	public void test_json_types() throws Exception {

        test_progammed_config("json_types");
	}

	public void test_json_map() throws Exception {

        test_progammed_config("json_map");
	}

	public void test_json_array() throws Exception {

        test_progammed_config("json_array");
	}

	public void test_json_map_array() throws Exception {

        test_progammed_config("json_map_array");
	}

	public void test_json_array_map() throws Exception {

        test_progammed_config("json_array_map");
	}

	public void test_json_map_array_map() throws Exception {

        test_progammed_config("json_map_array_map");
	}

    public void test_simple_smooks_config() throws Exception {
    	test_config_file("simple_smooks_config");
    }

    public void test_key_replacement() throws Exception {
    	test_config_file("key_replacement");
    }

    public void test_several_replacements() throws Exception {
    	test_config_file("several_replacements");
    }

    public void test_configured_different_node_names() throws Exception {
    	test_config_file("configured_different_node_names");
    }

	private void test_progammed_config(String testNumber) throws Exception{
		Smooks smooks = new Smooks();
		SmooksResourceConfiguration config;

        config = new SmooksResourceConfiguration("org.xml.sax.driver", "type:Order-List AND from:Acme", JSONReader.class.getName());
		SmooksUtil.registerResource(config, smooks);
		SmooksUtil.registerProfileSet(DefaultProfileSet.create("Order-List-Acme-AcmePartner1", new String[] {"type:Order-List", "from:Acme", "to:AcmePartner1"}), smooks);

		ExecutionContext context = smooks.createExecutionContext("Order-List-Acme-AcmePartner1");
		String result = SmooksUtil.filterAndSerialize(context,  getClass().getResourceAsStream("/test/"+ testNumber +"/input-message.jsn"), smooks);

        if(logger.isDebugEnabled()) {
        	logger.debug("Result: " + result);
        }

        assertEquals("/test/" + testNumber + "/expected.xml", result.getBytes());
	}

    private void test_config_file(String testNumber) throws Exception {
        Smooks smooks = new Smooks("/test/" + testNumber + "/smooks-config.xml");

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testNumber + "/input-message.jsn"), smooks);

        if(logger.isDebugEnabled()) {
        	logger.debug("Result: " + result);
        }

        assertEquals("/test/" + testNumber + "/expected.xml", result.getBytes());
    }

	private void assertEquals(String fileExpected, byte[] actual) throws IOException {

		byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream(fileExpected));

        assertTrue("Expected XML and result XML are not the same!", StreamUtils.compareCharStreams(new ByteArrayInputStream(actual), new ByteArrayInputStream(expected)));

	}
}
