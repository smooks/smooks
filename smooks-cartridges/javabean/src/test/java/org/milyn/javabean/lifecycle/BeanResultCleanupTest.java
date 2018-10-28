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
package org.milyn.javabean.lifecycle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.milyn.StreamFilterType;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class BeanResultCleanupTest {

    @Test
    public void test() throws IOException, SAXException {
        test(StreamFilterType.DOM);
        test(StreamFilterType.SAX);
    }

    private void test(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(new FilterSettings(filterType));
        smooks.filterSource(new StringSource("<root><a><b>1</b></a></root>"), result);
        assertNotNull(result.getBean("root"));
        assertNull(result.getBean("a"));
        assertNull(result.getBean("b"));
    }
}
