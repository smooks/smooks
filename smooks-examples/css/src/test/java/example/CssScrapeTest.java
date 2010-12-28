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
package example;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.xml.sax.SAXException;
import org.milyn.container.ExecutionContext;
import org.milyn.css.CSSAccessor;
import org.milyn.xml.XmlUtil;
import org.milyn.magger.CSSProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CssScrapeTest extends TestCase {

    public void test() throws IOException, SAXException {
        ExecutionContext executionContext = Main.runSmooksFilter();
        CSSAccessor cssAccessor = CSSAccessor.getInstance(executionContext);
        Document htmlDoc = XmlUtil.parseStream(new ByteArrayInputStream(Main.htmlIn), XmlUtil.VALIDATION_TYPE.NONE, false);

        Element htmlElement = (Element) XmlUtil.getNode(htmlDoc, "/html");
        CSSProperty property;

        property = cssAccessor.getProperty(htmlElement, "padding");
        assertNotNull(property);
        assertEquals(50, property.getValue().getIntegerValue());
        System.out.println("padding property: " + property.getValue());

        Element pElement = (Element) XmlUtil.getNode(htmlDoc, "/html/body/p");
        property = cssAccessor.getProperty(pElement, "margin");
        assertNotNull(property);
        assertEquals(10, property.getValue().getIntegerValue());
        System.out.println("margin property: " + property.getValue());
    }
}
