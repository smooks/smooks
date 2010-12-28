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
package org.milyn.javabean.JIRA.MILYN_364;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_364_Test extends TestCase {

    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(MILYN_364_Test.class.getResourceAsStream("config.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        
        try {
            StringResult xmlResult = new StringResult();
            JavaResult javaResult = new JavaResult();
            
            smooks.filterSource(new StreamSource(MILYN_364_Test.class.getResourceAsStream("order.xml")), xmlResult, javaResult);
            
            Header bean = (Header) javaResult.getBean("header");
            
            // Truncate to avoid rounding differences etc...
			assertEquals(81, (int)bean.getNetAmount());
			assertEquals(18, bean.getNetAmountObj().intValue());
			assertEquals(16, (int)bean.getTax());
			assertEquals(98, (int)bean.getTotalAmount());
        } finally {
            smooks.close();
        }
    }
}