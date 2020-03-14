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
package org.smooks.cdr.xsd11.readertests;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.AbstractParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ReaderConfigTest {

	@Test
    public void test_01() throws IOException, SAXException {
        // Should allow reader config without a defined reader class i.e. config the default... 
        new Smooks(getClass().getResourceAsStream("config_01.xml"));
    }

	@Test
    public void test_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));
        SmooksResourceConfiguration readerConfig = AbstractParser.getSAXParserConfiguration(smooks.createExecutionContext("A").getDeliveryConfig());

        assertEquals("com.ZZZZReader", readerConfig.getResource());

        assertEquals("A", readerConfig.getTargetProfile());

        List handlers = readerConfig.getParameters("sax-handler");
        assertEquals("[com.X, com.Y]", handlers.toString());

        List featuresOn = readerConfig.getParameters("feature-on");
        assertEquals("[http://a, http://b]", featuresOn.toString());

        List featuresOff = readerConfig.getParameters("feature-off");
        assertEquals("[http://c, http://d]", featuresOff.toString());

        assertEquals("val1", readerConfig.getStringParameter("param1"));
        assertEquals("val2", readerConfig.getStringParameter("param2"));
    }
}
