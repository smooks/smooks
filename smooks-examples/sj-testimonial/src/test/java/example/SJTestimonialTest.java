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

import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;
import se.sj.ipl.rollingstock.domain.RollingStockList;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SJTestimonialTest {

	@Test
    public void test_dom() throws IOException, SAXException {
        test("smooks-config.xml");
    }

	@Test
    public void test_sax() throws IOException, SAXException {
        test("smooks-config-sax.xml");
    }

    private void test(String config) throws IOException, SAXException {
        Map beans = Main.runSmooksTransform(config);
        RollingStockList rollingstocks = (RollingStockList) beans.get("rollingstocks");

        // Just some really basic checks
        assertEquals(3, rollingstocks.size());
        assertEquals(1, rollingstocks.get(0).getVehicles().size());
        assertEquals(2, rollingstocks.get(1).getVehicles().size());
        assertEquals(1, rollingstocks.get(2).getVehicles().size());
        assertEquals(1, rollingstocks.get(2).getVehicles().get(0).getComments().size());
    }
}
