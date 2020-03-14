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
package org.smooks.xml;

import java.io.IOException;
import java.util.Properties;

import org.smooks.Smooks;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class NamespaceMappingsTest {

	@Test
	public void test_01() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));		
		ExecutionContext execContext = smooks.createExecutionContext();
		ApplicationContext appContext = execContext.getContext();
		Properties mappings = NamespaceMappings.getMappings(appContext);

		assertEquals("http://a", mappings.getProperty("a"));
		assertEquals("http://b", mappings.getProperty("b"));
		assertEquals("http://c", mappings.getProperty("c"));
	}
}
