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
package org.milyn.cartridge.javabean.v14.getbytype;

import com.google.common.io.Resources;
import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.cartridge.javabean.extendedconfig.ExtendedOrder;
import org.milyn.cartridge.javabean.extendedconfig13.BeanBindingExtendedConfigTest;
import org.milyn.cartridge.javabean.v14.retain_bean.RetainBeanTest;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GetBeanByTypeTest extends TestCase {

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

    private InputStream getInput(String file) throws IOException {
        return Resources.getResource(ExtendedOrder.class, file).openStream();
    }
}
