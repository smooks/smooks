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
package org.milyn.delivery.java;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaSource;
import org.milyn.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaSourceTest extends TestCase {

    public void test_dom() throws IOException, SAXException {
        test("smooks-config-dom.xml", SOURCE_1, EXPECTED_1);
    }

    public void test_sax() throws IOException, SAXException {
        test("smooks-config-sax.xml", SOURCE_1, EXPECTED_1);
    }

    public void test_includeEnclosingDocument() throws IOException, SAXException {
        // Not sure what that "includeEnclosingDocument" flag on the XStream SaxWriter is supposed to do.
        // Seems to do the same thing whether it's on or off???...
        test("smooks-config-inc-encl-doc-on.xml", SOURCE_1, EXPECTED_1);
        test("smooks-config-inc-encl-doc-off.xml", SOURCE_1, EXPECTED_1);
        test("smooks-config-inc-encl-doc-on.xml", SOURCE_2, EXPECTED_2);
        test("smooks-config-inc-encl-doc-off.xml", SOURCE_2, EXPECTED_2);
    }

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

    public void test_streamingOff_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-off.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new JavaSource(new MyBean1()), result);
        assertEquals("<nullsource-document />", result.getResult());
    }

    public void test_streamingOff_02() throws IOException, SAXException {
        Smooks smooks = new Smooks();
        JavaSource javaSource = new JavaSource(new MyBean1());
        StringResult result = new StringResult();

        // Turn streaming off via the JavaSource...
        javaSource.setEventStreamRequired(false);

        smooks.filterSource(javaSource, result);
        assertEquals("<nullsource-document />", result.getResult());
    }

    public void test_streamingOn_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-on.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new JavaSource(new MyBean1()));
        assertNotSame("<nullsource-document></nullsource-document>", result.getResult());
    }

    public void test_streamingOn_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-eventstream-on.xml"));
        JavaSource javaSource = new JavaSource(new MyBean1());

        // Explicitly configure it on... should cause an erros because it is explicitly
        // configured 'on' in the config...
        javaSource.setEventStreamRequired(false);

        try {
            smooks.filterSource(javaSource);
        } catch (SmooksException e) {
            assertEquals("Invalid Smooks configuration.  Feature '" + JavaSource.FEATURE_GENERATE_EVENT_STREAM + "' is explicitly configured 'on' in the Smooks configuration, while the supplied JavaSource has explicitly configured event streaming to be off (through a call to JavaSource.setEventStreamRequired).", e.getCause().getMessage());
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

    private static List<Object> SOURCE_1;

    static {
        SOURCE_1 = new ArrayList();
        SOURCE_1.add(new MyBean1());
    }

    private static String EXPECTED_1 = "<org.milyn.delivery.java.MyBean1><prop1>true</prop1><prop2>hello</prop2><prop3>1111</prop3><mybean2><prop5>true</prop5><prop6>hello</prop6></mybean2></org.milyn.delivery.java.MyBean1>";

    private static List<Object> SOURCE_2 = Arrays.asList(new Object[]{new MyBean2(), new MyBean2()});
    private static String EXPECTED_2 = "<org.milyn.delivery.java.MyBean2><prop5>true</prop5><prop6>hello</prop6></org.milyn.delivery.java.MyBean2><org.milyn.delivery.java.MyBean2><prop5>true</prop5><prop6>hello</prop6></org.milyn.delivery.java.MyBean2>";
}
