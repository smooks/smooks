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
package org.milyn.routing.basic;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.javabean.repository.BeanRepository;
import org.milyn.payload.JavaResult;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FragmentSerializerTest extends TestCase {

    public void test_children_only_SAX() throws IOException, SAXException {
    	test_children_only(FilterSettings.DEFAULT_SAX);
    }
    public void test_children_only_DOM() throws IOException, SAXException {
    	test_children_only(FilterSettings.DEFAULT_DOM);
    }
    private void test_children_only(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        StreamSource source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(source, result);

        XMLUnit.setIgnoreWhitespace( true );
        InputStream stream = getClass().getResourceAsStream("children-only.xml");
        Object bean = result.getBean("soapBody");
        XMLAssert.assertXMLEqual(new InputStreamReader(stream), new StringReader(bean.toString().trim()));
    }

    public void test_all_SAX() throws IOException, SAXException {
    	test_all(FilterSettings.DEFAULT_SAX);
    }
    public void test_all_DOM() throws IOException, SAXException {
    	test_all(FilterSettings.DEFAULT_DOM);
    }
    private void test_all(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-02.xml"));
        StreamSource source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(source, result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("all.xml")), new StringReader(result.getBean("soapBody").toString().trim()));
    }
    
    public void test_multi_fragments_SAX() throws IOException, SAXException {
    	test_multi_fragments(FilterSettings.DEFAULT_SAX);
    }
    public void test_multi_fragments_DOM() throws IOException, SAXException {
    	test_multi_fragments(FilterSettings.DEFAULT_DOM);
    }    
    private void test_multi_fragments(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new FragmentSerializer().setBindTo("orderItem"), "order-items/order-item");
        MockRouter router = new MockRouter().setBoundTo("orderItem");
        smooks.addVisitor(router, "order-items/order-item");

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("input-message-02.xml")));
        assertEquals(2, router.routedObjects.size());

//        System.out.println(router.routedObjects.get(0));

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("frag1.xml")), new StringReader((String) router.routedObjects.get(0)));
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("frag2.xml")), new StringReader((String) router.routedObjects.get(1)));
    }
    
    private class MockRouter implements SAXVisitAfter, DOMVisitAfter {

        private String boundTo;
        private List<Object> routedObjects = new ArrayList<Object>();
    	
		public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
			routedObjects.add(BeanRepository.getInstance(executionContext).getBean(boundTo));
		}

		public void visitAfter(Element element,	ExecutionContext executionContext) throws SmooksException {
			routedObjects.add(BeanRepository.getInstance(executionContext).getBean(boundTo));
		}
		
		public MockRouter setBoundTo(String boundTo) {
			this.boundTo = boundTo;
			return this;
		}
    }
}
