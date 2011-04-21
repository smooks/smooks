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
package org.milyn.templating.freemarker;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.Smooks;
import org.milyn.StreamFilterType;
import org.milyn.FilterSettings;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.payload.JavaSource;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.templating.MyBean;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tfennelly
 */
public class FreeMarkerContentHandlerFactoryTest extends TestCase {

    public void testFreeMarkerTrans_01() throws SAXException, IOException {
        testFreeMarkerTrans_01("test-configs-01.cdrl");
        testFreeMarkerTrans_01("test-configs-01-SAX.cdrl");
    }

    public void test_nodeModel_1() throws IOException, SAXException {
        test_nodeModel_1(StreamFilterType.DOM);
        test_nodeModel_1(StreamFilterType.SAX);
    }
    public void test_nodeModel_1(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/test-configs-05.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));
        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>"), result);
        assertEquals("'cvalue1''cvalue2''cvalue3'", result.toString());
    }

    public void test_nodeModel_2() throws IOException, SAXException {
        test_nodeModel_2(StreamFilterType.DOM);
        test_nodeModel_2(StreamFilterType.SAX);
    }
    public void test_nodeModel_2(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/test-configs-06.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));
        test_ftl(smooks, "<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>", "<a><b><x>'cvalue1'</x><x>'cvalue2'</x><x>'cvalue3'</x></b></a>");
    }

    public void test_nodeModel_3() throws IOException, SAXException {
        test_nodeModel_3(StreamFilterType.DOM);
        test_nodeModel_3(StreamFilterType.SAX);
    }
    public void test_nodeModel_3(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/test-configs-07.cdrl");

        smooks.setFilterSettings(new FilterSettings(filterType));
        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><b javabind='javaval'><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>"), result);
        assertEquals("'cvalue1''cvalue2''cvalue3' javaVal=javaval", result.toString());
    }

    public void testFreeMarkerTrans_01(String config) throws SAXException, IOException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/" + config);

        test_ftl(smooks, "<a><b><c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a><b><mybean>xvalueonc1</mybean><mybean>xvalueonc2</mybean></b></a>");
        // Test transformation via the <context-object /> by transforming the root element using StringTemplate.
        test_ftl(smooks, "<c x='xvalueonc1' />", "<mybean>xvalueonc1</mybean>");
    }

    public void testFreeMarkerTrans_01_NS() throws SAXException, IOException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/test-configs-01-NS.cdrl");

        test_ftl(smooks, "<a xmlns:x=\"http://x\"><b><x:c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a xmlns:x=\"http://x\"><b><mybean>xvalueonc1</mybean><c x=\"xvalueonc2\"></c></b></a>");
    }

    public void testFreeMarkerTrans_02() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-02.cdrl"));

        test_ftl(smooks, "<a><b><c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a><b><mybean>xvalueonc1</mybean><mybean>xvalueonc2</mybean></b></a>");
        // Test transformation via the <context-object /> by transforming the root element using StringTemplate.
        test_ftl(smooks, "<c x='xvalueonc1' />", "<mybean>xvalueonc1</mybean>");
    }

    public void testFreeMarkerTrans_03() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-03.cdrl"));

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

    public void testFreeMarkerTrans_bind() throws SAXException, IOException {
        testFreeMarkerTrans_bind("test-configs-04.cdrl");
        testFreeMarkerTrans_bind("test-configs-04-SAX.cdrl");
    }
    public void testFreeMarkerTrans_bind(String config) throws SAXException, IOException {
        Smooks smooks = new Smooks("/org/milyn/templating/freemarker/" + config);
        StringReader input;
        ExecutionContext context;

        context = smooks.createExecutionContext();
        input = new StringReader("<a><b><c x='xvalueonc2' /></b></a>");
        smooks.filterSource(context, new StreamSource(input), null);

        assertEquals("<mybean>xvalueonc2</mybean>",context.getBeanContext().getBean("mybeanTemplate"));

        context = smooks.createExecutionContext();
        input = new StringReader("<c x='xvalueonc1' />");
        smooks.filterSource(context, new StreamSource(input), null);
        assertEquals("<mybean>xvalueonc1</mybean>", context.getBeanContext().getBean("mybeanTemplate"));
    }

    public void test_template_include() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-include.cdrl"));

        test_ftl(smooks, "<a><c/></a>",
                         "<a><maintemplate><included>blah</included></maintemplate></a>");
    }

    public void testInsertBefore() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-before.cdrl"));

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

    public void testInsertAfter() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-insert-after.cdrl"));

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

    public void testAddTo() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-addto.cdrl"));

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

    public void testReplace() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-replace.cdrl"));

        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\"></b><mybean>xvalueonc1</mybean><d></d></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-replace.cdrl"));
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c/><d/></a>",
                         "<a><b x=\"xvalueonc1\" /><mybean>xvalueonc1</mybean><d /></a>");

        smooks = new Smooks(getClass().getResourceAsStream("test-configs-replace.cdrl"));
        smooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX).setDefaultSerializationOn(false));
        test_ftl(smooks, "<a><b x='xvalueonc1' /><c>11<f/>11</c><d/></a>",
                         "<mybean>xvalueonc1</mybean>");
    }

    public void test_no_default_ser() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-configs-no-default-ser.cdrl"));

        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><e><b x='xvalueonc1' /><c/><d/><b x='xvalueonc2' /></e></a>"), result);
        assertEquals("<mybean>xvalueonc1</mybean><d /><mybean>xvalueonc2</mybean>", result.toString());
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
