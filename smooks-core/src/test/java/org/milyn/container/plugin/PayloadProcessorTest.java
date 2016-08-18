/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.container.plugin;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.javabean.context.preinstalled.Time;
import org.milyn.javabean.context.preinstalled.UniqueID;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

/**
 * Unit test for PayloadProcessor.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class PayloadProcessorTest {
    private Smooks smooks;

    @Before
    public void setup() throws IOException, SAXException {
        smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void process() throws IOException, SAXException {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        processor.process(null, smooks.createExecutionContext());
    }

    @Test
    public void process_SourceResult() throws IOException, SAXException {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        StreamSource source = new StreamSource(new StringReader("<text/>"));
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        SourceResult sourceResult = new SourceResult(source, result);
        Object object = processor.process(sourceResult, smooks.createExecutionContext());

        TestCase.assertEquals(result, object);
        TestCase.assertEquals("<text />", writer.toString());
    }

    @Test
    public void process_String2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process("<testing/>", smooks.createExecutionContext());

        TestCase.assertEquals("<testing />", object);
    }

    @Test
    public void process_bytes2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object payload = "<testing/>".getBytes();
        Object object = processor.process(payload, smooks.createExecutionContext());

        TestCase.assertEquals("<testing />", object);
    }

    @Test
    public void process_bytes2bytes() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.BYTES);
        Object payload = "<testing/>".getBytes();
        Object object = processor.process(payload, smooks.createExecutionContext());

        TestCase.assertTrue(object instanceof byte[]);
        TestCase.assertTrue(Arrays.equals("<testing />".getBytes(), ((byte[]) object)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void process_String2Java_01() {
        final PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.JAVA);
        Map<String, Object> map = (Map<String, Object>) processor.process("<testing/>", smooks.createExecutionContext());

        assertThat(map, hasEntry("theBean", (Object) "Hi there!"));
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

        TestCase.assertEquals("Hi there!", object.toString());
    }

    @Test
    public void process_Java2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(123, smooks.createExecutionContext());

        TestCase.assertEquals("<int>123</int>", object.toString());
    }

    @Test
    public void process_Reader2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new StringReader("<test/>"), smooks.createExecutionContext());

        TestCase.assertEquals("<test />", object.toString());
    }

    @Test
    public void process_Stream2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new ByteArrayInputStream("<test/>".getBytes()), smooks.createExecutionContext());

        TestCase.assertEquals("<test />", object.toString());
    }

    @Test
    public void process_Source2String() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.STRING);
        Object object = processor.process(new StreamSource(new ByteArrayInputStream("<test/>".getBytes())), smooks.createExecutionContext());

        TestCase.assertEquals("<test />", object.toString());
    }

    @Test
    public void process_NoResult() {
        PayloadProcessor processor = new PayloadProcessor(smooks, ResultType.NORESULT);
        Object object = processor.process(123, smooks.createExecutionContext());

        TestCase.assertEquals(null, object);
    }
}
