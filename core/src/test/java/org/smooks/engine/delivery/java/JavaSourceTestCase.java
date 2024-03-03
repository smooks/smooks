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
package org.smooks.engine.delivery.java;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.io.payload.JavaSource;
import org.smooks.io.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class JavaSourceTestCase {

	@Test
    public void test_dom() throws IOException, SAXException {
        test("smooks-config-dom.xml", SOURCE_1, EXPECTED_1);
    }

	@Test
    public void test_sax() throws IOException, SAXException {
        test("smooks-config-sax.xml", SOURCE_1, EXPECTED_1);
    }

	@Test
    public void test_includeEnclosingDocument() throws IOException, SAXException {
        // Not sure what that "includeEnclosingDocument" flag on the XStream SaxWriter is supposed to do.
        // Seems to do the same thing whether it's on or off???...
        test("smooks-config-inc-encl-doc-on.xml", SOURCE_1, EXPECTED_1);
        test("smooks-config-inc-encl-doc-off.xml", SOURCE_1, EXPECTED_1);
        test("smooks-config-inc-encl-doc-on.xml", SOURCE_2, EXPECTED_2);
        test("smooks-config-inc-encl-doc-off.xml", SOURCE_2, EXPECTED_2);
    }

	@Test
    public void test_beanSetting() {
        MyBean1 pojo = new MyBean1();
        JavaSource source;

        source = new JavaSource(pojo);
        assertEquals(pojo, source.getBeans().get("myBean1"));

        source = new JavaSource("blah", pojo);
        assertEquals(pojo, source.getBeans().get("blah"));

        Map beans = new HashMap();
        beans.put("abcd", pojo);
        source = new JavaSource(beans);
        assertEquals(pojo, source.getBeans().get("abcd"));
    }

	@Test
    public void test_streamingOff_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-off.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new JavaSource(new MyBean1()), result);
        assertEquals("<nullsource-document/>", result.getResult());
    }

    @Test
    public void test_streamingOff_02() {
        Smooks smooks = new Smooks();
        JavaSource javaSource = new JavaSource(new MyBean1());
        StringResult result = new StringResult();

        // Turn streaming off via the JavaSource...
        javaSource.setEventStreamRequired(false);

        smooks.filterSource(javaSource, result);
        assertEquals("<nullsource-document/>", result.getResult());
    }

	@Test
    public void test_streamingOn_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-on.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new JavaSource(new MyBean1()));
        assertNotSame("<nullsource-document></nullsource-document>", result.getResult());
    }

	@Test
    public void test_streamingOn_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-on.xml"));
        JavaSource javaSource = new JavaSource(new MyBean1());

        // Explicitly configure it on... should cause an erros because it is explicitly
        // configured 'on' in the config...
        javaSource.setEventStreamRequired(false);

        try {
            smooks.filterSource(javaSource);
        } catch(SmooksException e) {
            assertEquals("Invalid Smooks configuration. Feature [" + JavaSource.FEATURE_GENERATE_EVENT_STREAM + "] is explicitly configured 'on' in the Smooks configuration, while the supplied JavaSource has explicitly configured event streaming to be off (through a call to JavaSource.setEventStreamRequired).", e.getCause().getMessage());
        }
    }

    private void test(String config, List<Object> sourceObjects, String expected) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        JavaSource source = new JavaSource(sourceObjects);
        StringWriter result = new StringWriter();

        smooks.filterSource(execContext, source, new StreamResult(result));
        assertEquals(expected, result.toString());
    }

    private static final List<Object> SOURCE_1;
    static {
        SOURCE_1 = new ArrayList();
        SOURCE_1.add(new MyBean1());
    }
    private static final String EXPECTED_1 = "<org.smooks.engine.delivery.java.MyBean1><prop1>true</prop1><prop2>hello</prop2><prop3>1111</prop3><mybean2><prop5>true</prop5><prop6>hello</prop6></mybean2></org.smooks.engine.delivery.java.MyBean1>";

    private static final List<Object> SOURCE_2 = Arrays.asList(new Object[] {new MyBean2(), new MyBean2()});
    private static final String EXPECTED_2 = "<org.smooks.engine.delivery.java.MyBean2><prop5>true</prop5><prop6>hello</prop6></org.smooks.engine.delivery.java.MyBean2><org.smooks.engine.delivery.java.MyBean2><prop5>true</prop5><prop6>hello</prop6></org.smooks.engine.delivery.java.MyBean2>";
}
