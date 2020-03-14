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
package org.smooks.delivery.sax.annotation;

import java.io.IOException;
import java.io.Writer;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StreamResultWriterAndTextConsumerTest {

	@Test
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
