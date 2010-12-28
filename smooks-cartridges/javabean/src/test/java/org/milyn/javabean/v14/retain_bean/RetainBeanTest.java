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
package org.milyn.javabean.v14.retain_bean;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.extendedconfig.ExtendedOrder;
import org.milyn.javabean.extendedconfig13.BeanBindingExtendedConfigTest;
import org.milyn.payload.JavaResult;
import org.milyn.util.ClassUtil;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RetainBeanTest extends TestCase {

    public void test_01_DOM() throws IOException, SAXException {
    	test_01(FilterSettings.DEFAULT_DOM);
    }

    public void test_01_SAX() throws IOException, SAXException {
    	test_01(FilterSettings.DEFAULT_SAX);
    }

    public void test_01(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        //execContext.setEventListener(new HtmlReportGenerator("/zap/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        ExtendedOrder order = (ExtendedOrder) result.getBean("order");
        BeanBindingExtendedConfigTest.assertOrderOK(order, true);
        
        assertNull(result.getBean("headerBean"));
        assertNull(result.getBean("headerBeanHash"));
        assertNull(result.getBean("orderItemList"));
        assertNull(result.getBean("orderItemArray"));
        assertNull(result.getBean("orderItem"));
    }

    public void test_02_DOM() throws IOException, SAXException {
    	test_02(FilterSettings.DEFAULT_DOM);
    }

    public void test_02_SAX() throws IOException, SAXException {
    	test_02(FilterSettings.DEFAULT_SAX);
    }
    
    public void test_02(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_02.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        //execContext.setEventListener(new HtmlReportGenerator("/zap/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        ExtendedOrder order = (ExtendedOrder) result.getBean("order");
        BeanBindingExtendedConfigTest.assertOrderOK(order, true);
        
        assertNotNull(result.getBean("headerBean"));
        assertNull(result.getBean("headerBeanHash"));
        assertNull(result.getBean("orderItemList"));
        assertNull(result.getBean("orderItemArray"));
        assertNull(result.getBean("orderItem"));
    }

	private InputStream getInput(String file) {
		return ClassUtil.getResourceAsStream("/org/milyn/javabean/extendedconfig/" + file, this.getClass());
	}
}
