/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.PreconfiguredSmooks;
import org.smooks.engine.profile.DefaultProfileSet;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.StreamSource;
import org.smooks.support.SmooksUtil;
import org.smooks.support.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class SmooksStandaloneTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmooksStandaloneTestCase.class);
	
	@Test
    public void testProcess() throws IOException, SAXException, DocumentException {
        Smooks smooks = new PreconfiguredSmooks();
        ExecutionContext context = smooks.createExecutionContext("msie6");
        String response = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("html_2.html"), smooks);
        LOGGER.debug(response);
        Document doc = new DOMWriter().write(DocumentHelper.parseText(response));

        assertNull(XmlUtils.getNode(doc, "html/body/xxx"));
        assertNotNull(XmlUtils.getNode(doc, "html/body/yyy"));
    }

	@Test
    public void test_Standalone_CodeConfig_1() {
        Smooks smooks = new Smooks();

        // Add profile sets...
        SmooksUtil.registerProfileSet(new DefaultProfileSet("message-target1", new String[]{"profile1", "profile2"}), smooks);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("message-target2", new String[]{"profile2", "profile3"}), smooks);

        // Create CDU configs and target them at the profiles...
        ResourceConfig profile1AndNotProfile3ResourceConfig = new DefaultResourceConfig("ccc", new Properties(), "profile1 AND not:profile3", RenameElementTrans.class.getName());
        profile1AndNotProfile3ResourceConfig.setParameter("new-name", "xxx");
        smooks.getApplicationContext().getRegistry().registerResourceConfig(profile1AndNotProfile3ResourceConfig);

        ResourceConfig profile2ResourceConfig = new DefaultResourceConfig("aaa", new Properties(), "profile2", RenameElementTrans.class.getName());
        profile2ResourceConfig.setParameter("new-name", "zzz");
        smooks.getApplicationContext().getRegistry().registerResourceConfig(profile2ResourceConfig);

        // Transform the same message for each useragent...
        String message = "<aaa><bbb>888</bbb><ccc>999</ccc></aaa>";
        
        ExecutionContext messageTarget1ExecutionContext = smooks.createExecutionContext("message-target1");
        String messageTarget1Result = SmooksUtil.filterAndSerialize(messageTarget1ExecutionContext, new ByteArrayInputStream(message.getBytes()), smooks);
        assertEquals("<zzz><bbb>888</bbb><xxx>999</xxx></zzz>", messageTarget1Result, "Unexpected transformation result");

        ExecutionContext messageTarget2ExecutionContext = smooks.createExecutionContext("message-target2");
        String messageTarget2Result = SmooksUtil.filterAndSerialize(messageTarget2ExecutionContext, new ByteArrayInputStream(message.getBytes()), smooks);
        assertEquals("<zzz><bbb>888</bbb><ccc>999</ccc></zzz>", messageTarget2Result, "Unexpected transformation result");
    }

	@Test
    public void test_Standalone_CodeConfig_2() {
        Smooks smooks = new Smooks();

        // Add 2 useragents and configure them with profiles...
        SmooksUtil.registerProfileSet(new DefaultProfileSet("message-target1", new String[]{"profile1", "profile2"}), smooks);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("message-target2", new String[]{"profile2", "profile3"}), smooks);

        // Create CDU configs and target them at the profiles...
        ResourceConfig resourceConfig = new DefaultResourceConfig("ccc", new Properties(), "profile1 AND not:profile3", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "xxx");
        smooks.getApplicationContext().getRegistry().registerResourceConfig(resourceConfig);
        resourceConfig = new DefaultResourceConfig("aaa", new Properties(), "profile2", RenameElementTrans.class.getName());
        resourceConfig.setParameter("new-name", "zzz");
        smooks.getApplicationContext().getRegistry().registerResourceConfig(resourceConfig);

        // Transform the same message for each useragent...
        String message = "<aaa><bbb>888</bbb><ccc>999</ccc></aaa>";

        ExecutionContext context = smooks.createExecutionContext("message-target1");
        CharArrayWriter writer = new CharArrayWriter();
        smooks.filterSource(context, new StreamSource(new ByteArrayInputStream(message.getBytes())), new WriterSink(writer));

        assertEquals("<zzz><bbb>888</bbb><xxx>999</xxx></zzz>", writer.toString(), "Unexpected transformation result");
    }
    
}
