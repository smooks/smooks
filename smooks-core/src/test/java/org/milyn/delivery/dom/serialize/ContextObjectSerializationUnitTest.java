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
package org.milyn.delivery.dom.serialize;

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.cdr.ParameterAccessor;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Filter;
import org.milyn.xml.Namespace;

import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ContextObjectSerializationUnitTest {

	@Test
    public void test() {
        Smooks smooks = new Smooks();
        ExecutionContext context;

        ParameterAccessor.setParameter(Filter.STREAM_FILTER_TYPE, "DOM", smooks);

        context = smooks.createExecutionContext();
        context.setAttribute("object-x", "Hi there!");

        String result = SmooksUtil.filterAndSerialize(context, new ByteArrayInputStream(("<context-object key='object-x' xmlns=\"" + Namespace.SMOOKS_URI + "\" />").getBytes()), smooks);
        assertEquals("Hi there!", result);
    }
}
