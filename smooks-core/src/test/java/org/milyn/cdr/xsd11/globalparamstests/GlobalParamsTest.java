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
package org.milyn.cdr.xsd11.globalparamstests;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.payload.StringSource;
import org.milyn.container.ExecutionContext;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GlobalParamsTest extends TestCase {

    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_01.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        assertEquals("SAX", execContext.getConfigParameter("stream.filter.type"));
        assertEquals("zzzzval", execContext.getConfigParameter("zzzz"));
    }

    public void test_globalAnnotatedConfig_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));

        smooks.filterSource(new StringSource("<a/>"));
        assertEquals("blah", MyZapVisitor.configuredXP);
        assertEquals(1, MyZapVisitor.configuredZapCount);
    }

    public void test_globalAnnotatedConfig_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_03.xml"));

        smooks.filterSource(new StringSource("<a/>"));
        assertEquals("blah", MyZapVisitor.configuredXP);
        assertEquals(2, MyZapVisitor.configuredZapCount);
    }
}
