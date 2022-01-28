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
package org.smooks.engine.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.engine.bean.context.preinstalled.Time;
import org.smooks.engine.bean.context.preinstalled.UniqueID;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for PayloadProcessor.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class PayloadProcessorTestCase {
    private Smooks smooks;

    @BeforeEach
    public void setup() throws IOException, SAXException {
        smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
    }

    @Test
    public void process() throws IOException, SAXException {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        assertThrows(IllegalArgumentException.class, () -> processor.process(null, smooks.createExecutionContext()));
    }

    @Test
    public void process_SourceResult() throws IOException, SAXException {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        StreamSource source = new StreamSource(new StringReader("<text/>"));
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        SourceResult sourceResult = new SourceResult(source, result);
        Object object = processor.process(sourceResult, smooks.createExecutionContext());

        assertEquals(result, object);
        assertEquals("<text/>", writer.toString());
    }

    @Test
    public void process_String2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process("<testing/>", smooks.createExecutionContext());

        assertEquals("<testing/>", object);
    }

    @Test
    public void process_bytes2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object payload = "<testing/>".getBytes();
        Object object = processor.process(payload, smooks.createExecutionContext());

        assertEquals("<testing/>", object);
    }

    @Test
    public void process_bytes2bytes() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.BYTES);
        Object payload = "<testing/>".getBytes();
        Object object = processor.process(payload, smooks.createExecutionContext());

        assertTrue(object instanceof byte[]);
        assertTrue(Arrays.equals("<testing/>".getBytes(), ((byte[]) object)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void process_String2Java_01() {
        final PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.JAVA);
        Map<String, Object> map = (Map<String, Object>) processor.process("<testing/>", smooks.createExecutionContext());

        assertThat(map, hasEntry("theBean", "Hi there!"));
        assertThat(map, hasKey("PTIME"));
        assertThat(map, hasKey("PUUID"));

        assertThat(map.get("PTIME"), instanceOf(Time.class));
        assertThat(map.get("PUUID"), instanceOf(UniqueID.class));

    }


    @Test
    public void process_String2Java_02() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.JAVA);

        processor.setJavaResultBeanId("theBean");
        Object object = processor.process("<testing/>", smooks.createExecutionContext());

        assertEquals("Hi there!", object.toString());
    }

    @Test
    public void process_Java2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(123, smooks.createExecutionContext());

        assertEquals("<int>123</int>", object.toString());
    }

    @Test
    public void process_Reader2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new StringReader("<test/>"), smooks.createExecutionContext());

        assertEquals("<test/>", object.toString());
    }

    @Test
    public void process_Stream2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new ByteArrayInputStream("<test/>".getBytes()), smooks.createExecutionContext());

        assertEquals("<test/>", object.toString());
    }

    @Test
    public void process_Source2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new StreamSource(new ByteArrayInputStream("<test/>".getBytes())), smooks.createExecutionContext());

        assertEquals("<test/>", object.toString());
    }

    @Test
    public void process_NoResult() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.NORESULT);
        Object object = processor.process(123, smooks.createExecutionContext());

        assertNull(object);
    }
}
