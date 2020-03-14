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
package org.smooks.templating.freemarker;

import org.junit.Test;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.FilterSettings;
import org.smooks.container.ExecutionContext;
import org.smooks.payload.JavaSource;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.smooks.templating.MockOutStreamResource;
import org.smooks.templating.MyBean;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FreeMarkerContentHandlerFactoryExtendedConfigTest {

    @Test
    public void testFreeMarkerTrans_01() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-01.cdrl"));

        test_ftl(smooks, "<a><b><c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a><b><mybean>xvalueonc1</mybean><mybean>xvalueonc2</mybean></b></a>");
        // Test transformation via the <context-object /> by transforming the root element using StringTemplate.
        test_ftl(smooks, "<c x='xvalueonc1' />", "<mybean>xvalueonc1</mybean>");
    }

    @Test
    public void testFreeMarkerTrans_01_NS() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-01-NS.cdrl"));

        test_ftl(smooks, "<a xmlns:x=\"http://x\"><b><x:c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a xmlns:x=\"http://x\"><b><mybean>xvalueonc1</mybean><c x=\"xvalueonc2\"></c></b></a>");
    }

    @Test
    public void test_nodeModel_1() throws IOException, SAXException {
        test_nodeModel_1(StreamFilterType.DOM);
        test_nodeModel_1(StreamFilterType.SAX);
    }
    public void test_nodeModel_1(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/templating/freemarker/test-configs-ext-05.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));

        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>"), result);
        assertEquals("'cvalue1''cvalue2''cvalue3'", result.toString());
    }

    @Test
    public void test_nodeModel_2() throws IOException, SAXException {
        test_nodeModel_2(StreamFilterType.DOM);
        test_nodeModel_2(StreamFilterType.SAX);
    }
    public void test_nodeModel_2(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/templating/freemarker/test-configs-ext-06.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));
        test_ftl(smooks, "<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>", "<a><b><x>'cvalue1'</x><x>'cvalue2'</x><x>'cvalue3'</x></b></a>");
    }

    @Test
    public void test_nodeModel_3() throws IOException, SAXException {
        test_nodeModel_3(StreamFilterType.DOM);
        test_nodeModel_3(StreamFilterType.SAX);
    }
    public void test_nodeModel_3(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/templating/freemarker/test-configs-ext-07.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));
        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><b javabind='javaval'><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>"), result);
        assertEquals("'cvalue1''cvalue2''cvalue3' javaVal=javaval", result.toString());
    }

    @Test
    public void testFreeMarkerTrans_02_DOM() throws SAXException, IOException {
        testFreeMarkerTrans_02(FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void testFreeMarkerTrans_02_SAX() throws SAXException, IOException {
        testFreeMarkerTrans_02(FilterSettings.DEFAULT_SAX);
    }

    public void testFreeMarkerTrans_02(FilterSettings filterSettings) throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-02.cdrl"));

        smooks.setFilterSettings(filterSettings);

        test_ftl(smooks, "<a><b><c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a><b><mybean>xvalueonc1</mybean><mybean>xvalueonc2</mybean></b></a>");
        // Test transformation via the <context-object /> by transforming the root element using StringTemplate.
        test_ftl(smooks, "<c x='xvalueonc1' />", "<mybean>xvalueonc1</mybean>");
    }

    @Test
    public void testFreeMarkerTrans_03() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-03.cdrl"));

        // Initialise the input bean map...
        Map<String, Object> myBeans = new HashMap<String, Object>();
        MyBean myBean = new MyBean();
        myBean.setX("xxxxxxx");
        myBeans.put("myBeanData", myBean);

        JavaSource source = new JavaSource(myBeans);
        source.setEventStreamRequired(false);

        // Create the output writer for the transform and run it...
        StringWriter myTransformResult = new StringWriter();
        smooks.filterSource(smooks.createExecutionContext(), source, new StreamResult(myTransformResult));

        // Check it...
        assertEquals("<mybean>xxxxxxx</mybean>", myTransformResult.toString());
    }

    @Test
    public void testFreeMarkerTrans_bind_DOM() throws SAXException, IOException {
        testFreeMarkerTrans_bind(FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void testFreeMarkerTrans_bind_SAX() throws SAXException, IOException {
        testFreeMarkerTrans_bind(FilterSettings.DEFAULT_SAX);
    }

    public void testFreeMarkerTrans_bind(FilterSettings filterSettings) throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-04.cdrl"));
        StringReader input;
        ExecutionContext context;

        smooks.setFilterSettings(filterSettings);

        context = smooks.createExecutionContext();
        input = new StringReader("<a><b><c x='xvalueonc2' /></b></a>");
        smooks.filterSource(context, new StreamSource(input));

        assertEquals("<mybean>xvalueonc2</mybean>", context.getBeanContext().getBean("mybeanTemplate"));

        context = smooks.createExecutionContext();
        input = new StringReader("<c x='xvalueonc1' />");
        smooks.filterSource(context, new StreamSource(input), null);
        assertEquals("<mybean>xvalueonc1</mybean>", context.getBeanContext().getBean("mybeanTemplate"));
    }

    @Test
    public void test_template_include() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-include.cdrl"));

        test_ftl(smooks, "<a><c/></a>",
                         "<a><maintemplate><included>blah</included></maintemplate></a>");
    }

    @Test
    public void testInsertBefore() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-insert-before.cdrl"));

        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\"></b><mybean>xvalueonc1</mybean><c></c><d></d></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-before.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><mybean>xvalueonc1</mybean><c /><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-before.cdrl"));
        smooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX).setDefaultSerializationOn(false));
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>11<f/>11</c><d/></a>",
                         "<mybean>xvalueonc1</mybean>");
    }

    @Test
    public void testInsertAfter() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-insert-after.cdrl"));

        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\"></b><c></c><mybean>xvalueonc1</mybean><d></d></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-after.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><c /><mybean>xvalueonc1</mybean><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-after.cdrl"));
        smooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX).setDefaultSerializationOn(false));
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>11<f/>11</c><d/></a>",
                         "<mybean>xvalueonc1</mybean>");
    }

    @Test
    public void testAddTo() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-addto.cdrl"));

        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\"></b><c><mybean>xvalueonc1</mybean></c><d></d></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><c><mybean>xvalueonc1</mybean></c><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>1111</c><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><c>1111<mybean>xvalueonc1</mybean></c><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c><f/></c><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><c><f /><mybean>xvalueonc1</mybean></c><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>11<f/>11</c><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><c>11<f />11<mybean>xvalueonc1</mybean></c><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));
        smooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX).setDefaultSerializationOn(false));
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>11<f/>11</c><d/></a>",
                         "<mybean>xvalueonc1</mybean>");
    }

    @Test
    public void test_outputTo_Stream() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-outputToOutStream.cdrl"));
        ExecutionContext context = smooks.createExecutionContext();

        MockOutStreamResource.outputStream = new ByteArrayOutputStream();
        smooks.filterSource(context, new StringSource("<a/>"), null);

        assertEquals("data to outstream", MockOutStreamResource.outputStream.toString());
    }

    @Test
    public void test_PTIME() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-PTIME.cdrl"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<doc/>"), result);

        // should be able to convert the result to a Long instance...
        new Long(result.toString());
    }

    @Test
    public void test_PUUID() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-ext-PUUID.cdrl"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<doc/>"), result);
        assertTrue(result.toString().length() > 10);
    }

    private void test_ftl(Smooks smooks, String input, String expected) throws IOException, SAXException {
        ExecutionContext context = smooks.createExecutionContext();
        test_ftl(smooks, context, input, expected);
    }

    private void test_ftl(Smooks smooks, ExecutionContext context, String input, String expected) throws IOException, SAXException {
        StringResult result = new StringResult();

        smooks.filterSource(context, new StringSource(input), result);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(expected, result.getResult());
    }
}
