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
package org.milyn.calc;

import static org.testng.AssertJUnit.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Unit test for the extended configuration of the Counter class
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class CounterExtendedConfigTest {

	@Test ( groups = "unit" )
    public void test_before_full_extended_config() throws ParserConfigurationException, SAXException, IOException   {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-beforeVisit-full-extended-config.xml"));

		ExecutionContext executionContext = smooks.createExecutionContext();

		JavaResult result = new JavaResult();

		smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("test.xml")), result);

		Long a = (Long) result.getBean("a");

		assertNotNull(a);

		assertEquals(21, a.longValue());

		Long b = (Long) result.getBean("b");

		assertNotNull(b);

		assertEquals(5, b.longValue());

    }

}
