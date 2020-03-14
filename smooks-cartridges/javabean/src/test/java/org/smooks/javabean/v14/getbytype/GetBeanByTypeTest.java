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
package org.smooks.javabean.v14.getbytype;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.javabean.extendedconfig.ExtendedOrder;
import org.smooks.javabean.extendedconfig13.BeanBindingExtendedConfigTest;
import org.smooks.javabean.v14.retain_bean.RetainBeanTest;
import org.smooks.payload.JavaResult;
import org.smooks.util.ClassUtil;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GetBeanByTypeTest {

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(RetainBeanTest.class.getResourceAsStream("test_bean_01.xml"));
        JavaResult result = new JavaResult();

        ExecutionContext execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        ExtendedOrder order = result.getBean(ExtendedOrder.class);
        assertNotNull(order);
        BeanBindingExtendedConfigTest.assertOrderOK(order, true);
        
        order = execContext.getBeanContext().getBean(ExtendedOrder.class);
        assertNotNull(order);
        BeanBindingExtendedConfigTest.assertOrderOK(order, true);
    }
	private InputStream getInput(String file) {
		return ClassUtil.getResourceAsStream("/org/smooks/javabean/extendedconfig/" + file, this.getClass());
	}
}
