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
package org.smooks.javabean.JIRA.MILYN_451;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.javabean.context.BeanContext;
import org.smooks.payload.JavaResult;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_451_Test {

        @Test
	public void test() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
		ExecutionContext execCtx = smooks.createExecutionContext();
		BeanContext beanContext = execCtx.getBeanContext();
		JavaResult jResult = new JavaResult();
		
		smooks.filterSource(execCtx, new StreamSource(getClass().getResourceAsStream("message.xml")), jResult);
		assertEquals(3, jResult.getResultMap().size());
	}
}
