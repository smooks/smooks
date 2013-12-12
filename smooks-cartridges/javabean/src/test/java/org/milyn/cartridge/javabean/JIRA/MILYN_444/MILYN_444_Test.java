/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.cartridge.javabean.JIRA.MILYN_444;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_444_Test extends TestCase {

	public void test() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
		JavaResult jResult = new JavaResult();

		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("message.xml")), jResult);

        X x = jResult.getBean(X.class);
        assertEquals("456", x.getVal1()); // default will be overridden by value in the message
        assertEquals(987, x.getVal2()); // default will be applied
        assertEquals(99.65d, x.getVal3()); // default will be applied
	}
}