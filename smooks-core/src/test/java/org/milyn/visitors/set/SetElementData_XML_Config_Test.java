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
package org.milyn.visitors.set;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.visitors.remove.RemoveAttribute;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetElementData_XML_Config_Test extends TestCase {

    public void test_ChangeName_SAX() throws IOException, SAXException {
        test_ChangeName(FilterSettings.DEFAULT_SAX);
    }

    public void test_ChangeName_DOM() throws IOException, SAXException {
        test_ChangeName(FilterSettings.DEFAULT_DOM);
    }

    private void test_ChangeName(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><z>some text</z></d></c></a>"), new StringReader(result.getResult()));
    }

    public void test_SetNamespace_SAX() throws IOException, SAXException {
        test_SetNamespace(FilterSettings.DEFAULT_SAX);
    }

    public void test_SetNamespace_DOM() throws IOException, SAXException {
        test_SetNamespace(FilterSettings.DEFAULT_DOM);
    }

    private void test_SetNamespace(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-02.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), new StringReader(result.getResult()));
    }

    public void test_ChangeNamespace_1_SAX() throws IOException, SAXException {
        test_ChangeNamespace_1(FilterSettings.DEFAULT_SAX);
    }

    public void test_ChangeNamespace_1_DOM() throws IOException, SAXException {
        test_ChangeNamespace_1(FilterSettings.DEFAULT_DOM);
    }

    public void test_ChangeNamespace_1(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-03.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><yyy:z xmlns:yyy=\"http://yyy\">some text</yyy:z></d></c></a>"), new StringReader(result.getResult()));
    }

    public void test_ChangeNamespace_2_SAX() throws IOException, SAXException {
        test_ChangeNamespace_2(FilterSettings.DEFAULT_SAX);
    }

    public void test_ChangeNamespace_2_DOM() throws IOException, SAXException {
        test_ChangeNamespace_2(FilterSettings.DEFAULT_DOM);
    }

    public void test_ChangeNamespace_2(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-04.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><xxx:z xmlns:xxx=\"http://yyy\">some text</xxx:z></d></c></a>"), new StringReader(result.getResult()));
    }

    public void test_SetAttribute_1_SAX() throws IOException, SAXException {
        test_SetAttribute_1(FilterSettings.DEFAULT_SAX);
    }

    public void test_SetAttribute_1_DOM() throws IOException, SAXException {
        test_SetAttribute_1(FilterSettings.DEFAULT_DOM);
    }

    public void test_SetAttribute_1(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-05.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("injectedVal", "something");

        smooks.filterSource(execContext, new StringSource("<a/>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a xxx=\"something\" />"), new StringReader(result.getResult()));
    }

    public void test_SetAttribute_2_SAX() throws IOException, SAXException {
        test_SetAttribute_2(FilterSettings.DEFAULT_SAX);
    }

    public void test_SetAttribute_2_DOM() throws IOException, SAXException {
        test_SetAttribute_2(FilterSettings.DEFAULT_DOM);
    }

    public void test_SetAttribute_2(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-06.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("injectedVal", "something");

        smooks.filterSource(execContext, new StringSource("<a/>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a xmlns:ns1=\"http://ns1\" ns1:xxx=\"something\" />"), new StringReader(result.getResult()));
    }
}
