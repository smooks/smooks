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

package org.milyn;

import org.junit.Test;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.container.standalone.PreconfiguredSmooks;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.util.DomUtil;
import org.milyn.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class SmooksStandaloneTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmooksStandaloneTest.class);
	
	@Test
    public void testProcess() {
        Smooks smooks = null;
        try {
            smooks = new PreconfiguredSmooks();
            ExecutionContext context = smooks.createExecutionContext("msie6");
            String response = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("html_2.html"), smooks);
            LOGGER.debug(response);
            Document doc = DomUtil.parse(response);

            assertNull(XmlUtil.getNode(doc, "html/body/xxx"));
            assertNotNull(XmlUtil.getNode(doc, "html/body/yyy"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

	@Test
    public void test_Standalone_CodeConfig_1() {
        Smooks smooks = new Smooks();

        // Add profile sets...
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("message-target1", new String[]{"profile1", "profile2"}), smooks);
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("message-target2", new String[]{"profile2", "profile3"}), smooks);

        // Create CDU configs and target them at the profiles...
        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("ccc", "profile1 AND not:profile3", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "xxx");
        SmooksUtil.registerResource(resourceConfig, smooks);
        resourceConfig = new SmooksResourceConfiguration("aaa", "profile2", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "zzz");
        SmooksUtil.registerResource(resourceConfig, smooks);

        // Transform the same message for each useragent...
        String message = "<aaa><bbb>888</bbb><ccc>999</ccc></aaa>";
        ExecutionContext context = smooks.createExecutionContext("message-target1");
        String result = SmooksUtil.filterAndSerialize(context, new ByteArrayInputStream(message.getBytes()), smooks);
        LOGGER.debug(result);
        assertEquals("Unexpected transformation result", "<zzz><bbb>888</bbb><xxx>999</xxx></zzz>", result);
        context = smooks.createExecutionContext("message-target2");
        result = SmooksUtil.filterAndSerialize(context, new ByteArrayInputStream(message.getBytes()), smooks);
        LOGGER.debug(result);
        assertEquals("Unexpected transformation result", "<zzz><bbb>888</bbb><ccc>999</ccc></zzz>", result);
    }

	@Test
    public void test_Standalone_CodeConfig_2() throws SAXException, IOException {
        Smooks smooks = new Smooks();

        // Add 2 useragents and configure them with profiles...
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("message-target1", new String[]{"profile1", "profile2"}), smooks);
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("message-target2", new String[]{"profile2", "profile3"}), smooks);

        // Create CDU configs and target them at the profiles...
        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("ccc", "profile1 AND not:profile3", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "xxx");
        SmooksUtil.registerResource(resourceConfig, smooks);
        resourceConfig = new SmooksResourceConfiguration("aaa", "profile2", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "zzz");
        SmooksUtil.registerResource(resourceConfig, smooks);

        // Transform the same message for each useragent...
        String message = "<aaa><bbb>888</bbb><ccc>999</ccc></aaa>";

        ExecutionContext context = smooks.createExecutionContext("message-target1");
        CharArrayWriter writer = new CharArrayWriter();
        smooks.filterSource(context, new StreamSource(new ByteArrayInputStream(message.getBytes())), new StreamResult(writer));

        assertEquals("Unexpected transformation result", "<zzz><bbb>888</bbb><xxx>999</xxx></zzz>", writer.toString());
    }
    
}
