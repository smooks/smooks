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
package org.smooks.javabean.autodecode;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.javabean.OrderItem;
import org.smooks.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class AutoDecodeTest {

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        JavaResult jres = new JavaResult();

        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("../order-01.xml")), jres);
        } finally {
            smooks.close();
        }

        OrderItem orderItem = (OrderItem) jres.getBean("orderItem");

        assertEquals(222, orderItem.getProductId());
        assertEquals(7, (int)orderItem.getQuantity());
        assertEquals(5.2, orderItem.getPrice(), 0d);
    }
}
