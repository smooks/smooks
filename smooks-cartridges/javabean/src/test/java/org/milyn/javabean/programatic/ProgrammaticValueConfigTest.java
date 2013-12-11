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
package org.milyn.javabean.programatic;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.javabean.decoders.BooleanDecoder;
import org.milyn.commons.javabean.decoders.IntegerDecoder;
import org.milyn.javabean.Value;
import org.milyn.payload.JavaResult;

import javax.xml.transform.stream.StreamSource;

/**
 * Programmatic Binding config test for the Value class.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class ProgrammaticValueConfigTest extends TestCase {

    public void test_01() {

        Smooks smooks = new Smooks();

        smooks.addVisitor(new Value("customerName", "customer"));
        smooks.addVisitor(new Value("customerNumber", "customer/@number")
                .setDecoder(new IntegerDecoder()));
        smooks.addVisitor(new Value("privatePerson", "privatePerson")
                .setDecoder(new BooleanDecoder())
                .setDefaultValue("true"));

        JavaResult result = new JavaResult();
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("../order-01.xml")), result);

        assertEquals("Joe", result.getBean("customerName"));
        assertEquals(123123, result.getBean("customerNumber"));
        assertEquals(Boolean.TRUE, result.getBean("privatePerson"));
    }

    public void test_02() {

        Smooks smooks = new Smooks();

        smooks.addVisitor(new Value("customerNumber1", "customer/@number", Integer.class));
        smooks.addVisitor(new Value("customerNumber2", "customer/@number").setType(Integer.class));

        JavaResult result = new JavaResult();
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("../order-01.xml")), result);

        assertEquals(123123, result.getBean("customerNumber1"));
        assertEquals(123123, result.getBean("customerNumber2"));
    }

}
