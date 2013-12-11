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
package org.milyn.delivery.sax.annotation;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StreamResultWriterAndTextConsumerTest extends TestCase {

    public void test() {
        Smooks smooks = new Smooks();
        StringResult stringResult = new StringResult();

        smooks.addVisitor(new MyAnnotatedVisitor(), "b");
        smooks.filterSource(new StringSource("<a><b>sometext</b></a>"), stringResult);

        assertEquals("<a>{{sometext}}</a>", stringResult.getResult());
    }

    @TextConsumer
    @StreamResultWriter
    private class MyAnnotatedVisitor implements SAXVisitAfter {

        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            Writer writer = element.getWriter(this);
            writer.write("{{" + element.getTextContent() + "}}");
        }
    }
}
