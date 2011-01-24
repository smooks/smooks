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

package org.milyn.templating.xslt;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.SmooksUtil;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.templating.util.CharUtils;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 *
 * @author tfennelly
 */
public class XslContentHandlerFactoryTest extends TestCase {

	public void testXslUnitTrans_filebased_replace() {
		Smooks smooks = new Smooks();
		SmooksResourceConfiguration res = new SmooksResourceConfiguration("p", "org/milyn/templating/xslt/xsltransunit.xsl");
		String transResult = null;

        System.setProperty("javax.xml.transform.TransformerFactory", org.apache.xalan.processor.TransformerFactoryImpl.class.getName());
		SmooksUtil.registerResource(res, smooks);

		try {
			InputStream stream = getClass().getResourceAsStream("htmlpage.html");
            ExecutionContext context = smooks.createExecutionContext();
			transResult = SmooksUtil.filterAndSerialize(context, stream, smooks);
		} catch (SmooksException e) {
			e.printStackTrace();
			fail("unexpected exception: " + e.getMessage());
		}
		CharUtils.assertEquals("XSL Comparison Failure - See xsltransunit.expected1.", "/org/milyn/templating/xslt/xsltransunit.expected1", transResult);
	}

	public void testXslUnitTrans_parambased() {
		testXslUnitTrans_parambased("insertbefore", "xsltransunit.expected2");
		testXslUnitTrans_parambased("insertafter", "xsltransunit.expected3");
		testXslUnitTrans_parambased("addto", "xsltransunit.expected4");
		testXslUnitTrans_parambased("replace", "xsltransunit.expected5");
	}

	public void testXslUnitTrans_parambased(String action, String expectedFileName) {
		Smooks smooks = new Smooks();
		SmooksResourceConfiguration res = new SmooksResourceConfiguration("p", "<z id=\"{@id}\">Content from template!!</z>");
		String transResult = null;

		System.setProperty("javax.xml.transform.TransformerFactory", org.apache.xalan.processor.TransformerFactoryImpl.class.getName());

		res.setResourceType("xsl");
        res.setParameter(XslContentHandlerFactory.IS_XSLT_TEMPLATELET, "true");
		res.setParameter("action", action);
		SmooksUtil.registerResource(res, smooks);

		try {
			InputStream stream = getClass().getResourceAsStream("htmlpage.html");
            ExecutionContext context = smooks.createExecutionContext();
			transResult = SmooksUtil.filterAndSerialize(context, stream, smooks);
		} catch (SmooksException e) {
			e.printStackTrace();
			fail("unexpected exception: " + e.getMessage());
		}
		CharUtils.assertEquals("XSL Comparison Failure.  action=" + action + ".  See " + expectedFileName, "/org/milyn/templating/xslt/" + expectedFileName, transResult);
	}

    public void test_xsl_bind() throws SAXException, IOException {
        test_xsl_bind("test-configs-bind.cdrl");
        test_xsl_bind("test-configs-bind-ext.cdrl");
    }

    public void test_xsl_bind(String config) throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        StringReader input;
        ExecutionContext context;

        input = new StringReader("<a><b><c/></b></a>");
        context = smooks.createExecutionContext();
        smooks.filterSource(context, new StreamSource(input), null);

        assertEquals("<bind/>", context.getBeanContext().getBean("mybeanTemplate"));

        input = new StringReader("<c/>");
        context = smooks.createExecutionContext();
        smooks.filterSource(context, new StreamSource(input), null);
        assertEquals("<bind/>", context.getBeanContext().getBean("mybeanTemplate"));
    }

    public void test_inline_01() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("inline-01.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a/>"), result);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xxxxxx/>", result.getResult());
    }

    public void test_inline_xsl_function() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("inline-xsl.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a name='kalle'/>"), result);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><x>kalle</x>", result.getResult());
    }

    public void test_inline_02() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("inline-02.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a/>"), result);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>Hi there!", result.getResult());
    }

    public void test_inline_03() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("inline-03.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a/>"), result);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xxxxxx/>", result.getResult());
    }

    public void test_badxsl() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("bad-xsl-config.xml"));

        try {
            smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<doc/>")), null);
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Error loading Templating resource: Target Profile: [[org.milyn.profile.Profile#default_profile]], Selector: [#document], Selector Namespace URI: [null], Resource: [/org/milyn/templating/xslt/bad-stylesheet.xsl], Num Params: [0]", e.getCause().getMessage());
        }
    }
}
