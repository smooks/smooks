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
import org.milyn.delivery.DomModelCreator;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.javabean.Bean;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.templating.*;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FreeMarkerProgramaticConfigTest extends TestCase {

    public void testFreeMarkerTrans_01() throws SAXException, IOException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new Bean(MyBean.class, "myBeanData", "c").bindTo("x", "c/@x"));
        smooks.addVisitor(new FreeMarkerTemplateProcessor(new TemplatingConfiguration("/org/milyn/templating/freemarker/test-template.ftl")),"c");

        test_ftl(smooks, "<a><b><c x='xvalueonc1' /><c x='xvalueonc2' /></b></a>", "<a><b><mybean>xvalueonc1</mybean><mybean>xvalueonc2</mybean></b></a>");
        // Test transformation via the <context-object /> by transforming the root element using StringTemplate.
        test_ftl(smooks, "<c x='xvalueonc1' />", "<mybean>xvalueonc1</mybean>");
    }

    public void test_nodeModel_1() throws IOException, SAXException, ParserConfigurationException {
        test_nodeModel_1(StreamFilterType.DOM);
        test_nodeModel_1(StreamFilterType.SAX);
    }
    public void test_nodeModel_1(StreamFilterType filterType) throws IOException, SAXException, ParserConfigurationException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new DomModelCreator(), "$document");
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(new TemplatingConfiguration("<#foreach c in a.b.c>'${c}'</#foreach>")),
                "$document"
        );

        smooks.setFilterSettings(new FilterSettings(filterType));

        StringResult result = new StringResult();
        smooks.filterSource(new StringSource("<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>"), result);
        assertEquals("'cvalue1''cvalue2''cvalue3'", result.getResult());
    }

    public void test_nodeModel_2() throws IOException, SAXException, ParserConfigurationException {
        test_nodeModel_2(StreamFilterType.DOM);
        test_nodeModel_2(StreamFilterType.SAX);
    }
    public void test_nodeModel_2(StreamFilterType filterType) throws IOException, SAXException, ParserConfigurationException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new DomModelCreator(), "c");
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(new TemplatingConfiguration("<x>'${c}'</x>")),
                "c"
        );

        smooks.setFilterSettings(new FilterSettings(filterType));
        test_ftl(smooks, "<a><b><c>cvalue1</c><c>cvalue2</c><c>cvalue3</c></b></a>", "<a><b><x>'cvalue1'</x><x>'cvalue2'</x><x>'cvalue3'</x></b></a>");
    }

    public void testFreeMarkerTrans_bind() throws SAXException, IOException {
        StringReader input;
        ExecutionContext context;

        Smooks smooks = new Smooks();

        smooks.addVisitor(new Bean(MyBean.class, "myBeanData", "c").bindTo("x", "c/@x"));
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(
                        new TemplatingConfiguration("<mybean>${myBeanData.x}</mybean>").setUsage(BindTo.beanId("mybeanTemplate"))
                ),
                "c"
        );

        context = smooks.createExecutionContext();
        input = new StringReader("<a><b><c x='xvalueonc2' /></b></a>");
        smooks.filterSource(context, new StreamSource(input), null);

        assertEquals("<mybean>xvalueonc2</mybean>", context.getBeanContext().getBean("mybeanTemplate"));

        context = smooks.createExecutionContext();
        input = new StringReader("<c x='xvalueonc1' />");
        smooks.filterSource(context, new StreamSource(input), null);
        assertEquals("<mybean>xvalueonc1</mybean>", context.getBeanContext().getBean("mybeanTemplate"));
    }

    public void testInsertBefore() throws SAXException, IOException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new Bean(MyBean.class, "myBeanData", "b").bindTo("x", "b/@x"));
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(
                        new TemplatingConfiguration("/org/milyn/templating/freemarker/test-template.ftl").setUsage(Inline.INSERT_BEFORE)
                ),
                "c"
        );

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
        Smooks smooks = new Smooks();

        smooks.addVisitor(new Bean(MyBean.class, "myBeanData", "b").bindTo("x", "b/@x"));
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(
                        new TemplatingConfiguration("/org/milyn/templating/freemarker/test-template.ftl").setUsage(Inline.INSERT_AFTER)
                ),
                "c"
        );

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
        Smooks smooks = new Smooks();

        smooks.addVisitor(new Bean(MyBean.class, "myBeanData", "b").bindTo("x", "b/@x"));
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(
                        new TemplatingConfiguration("/org/milyn/templating/freemarker/test-template.ftl").setUsage(Inline.ADDTO)
                ),
                "c"
        );

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

    public void test_outputTo_Stream() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new MockOutStreamResource("outRes"), "$document");
        smooks.addVisitor(
                new FreeMarkerTemplateProcessor(
                        new TemplatingConfiguration("data to outstream").setUsage(OutputTo.stream("outRes"))
                ),
                "$document"
        );

        ExecutionContext context = smooks.createExecutionContext();

        MockOutStreamResource.outputStream = new ByteArrayOutputStream();
        smooks.filterSource(context, new StringSource("<a/>"), null);

        assertEquals("data to outstream", MockOutStreamResource.outputStream.toString());
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