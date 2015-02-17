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
package org.milyn.javabean.JIRA.MILYN_356;

import java.io.IOException;
import java.math.BigDecimal;

import org.milyn.Smooks;
import org.milyn.javabean.OrderItem;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_356_Test {

        @Test
	public void test_decoder_defined() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("config_01.xml"));
		JavaResult javaResult = new JavaResult();
		
		smooks.filterSource(new StringSource("<price>123'456,00</price>"), javaResult);
		
		OrderItem orderItem = (OrderItem) javaResult.getBean("orderItem");		
		assertEquals(123456.00D, orderItem.getPrice(),0D);

		BigDecimal baseBigD = (BigDecimal) javaResult.getBean("price");
		assertEquals(new BigDecimal("123456.00"), baseBigD);
	}

        @Test
	public void test_decoder_undefined() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));
		JavaResult javaResult = new JavaResult();
		
		smooks.filterSource(new StringSource("<price>123'456,00</price>"), javaResult);
		
		OrderItem orderItem = (OrderItem) javaResult.getBean("orderItem");		
		assertEquals(123456.00D, orderItem.getPrice(), 0D);
	}

}
