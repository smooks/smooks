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
package org.milyn.cartridge.javabean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.payload.JavaResult;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.commons.io.StreamUtils;
import org.milyn.commons.util.ClassUtil;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanBindingPopulatorTest extends TestCase {

	private static boolean REPORT_EXECUTION = true;

	private static final Log logger = LogFactory.getLog(BeanBindingPopulatorTest.class);

	public void test_01_hierarchically() throws IOException, SAXException {
		String xml = "bb-01-hierarchically.xml";

        test_01_list("bb-01-smooks-config.xml", xml);
        test_01_list("bb-01-smooks-config-sax.xml", xml);
		test_01_array("bb-01-smooks-config-array.xml", xml);
	}

	public void test_01_flat() throws IOException, SAXException {
		String xml = "bb-01-flat.xml";

		test_01_list("bb-01-smooks-config.xml", xml);
		test_01_list("bb-01-smooks-config-sax.xml", xml);
	}

	/**
	 * @param configFile
	 * @param dataFile
	 * @throws IOException
	 * @throws SAXException
	 */
	private void test_01_list(String configFile, String dataFile)
			throws IOException, SAXException {
		String packagePath = ClassUtil.toFilePath(getClass().getPackage());
        Smooks smooks = new Smooks(packagePath + "/" + configFile);
        ExecutionContext executionContext = smooks.createExecutionContext();
        
        if(REPORT_EXECUTION) {
        	executionContext.setEventListener(new HtmlReportGenerator("target/report/" + dataFile + "-" + configFile + "/index.html"));
        }
        
    	String resource = StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream(dataFile)));
    	JavaResult result = new JavaResult();

        smooks.filterSource(executionContext, new StreamSource(new StringReader(resource)), result);

        @SuppressWarnings("unchecked")
        ArrayList<A> as = (ArrayList<A>) result.getBean("root");

        assertNotNull(as);
        assertEquals(2, as.size());

		A a1 = as.get(0);
		assertNotNull(a1.getBList());
		assertEquals(3, a1.getBList().size());
		assertEquals("b1", a1.getBList().get(0).getValue());
		assertEquals(a1, a1.getBList().get(0).getA());

		A a2 = as.get(1);
		assertNotNull(a2.getBList());
		assertEquals(3, a2.getBList().size());
		assertEquals("b4", a2.getBList().get(0).getValue());
		assertEquals(a2, a2.getBList().get(0).getA());


	}

	/**
	 * @param configFile
	 * @param dataFile
	 * @throws IOException
	 * @throws SAXException
	 */
	private void test_01_array(String configFile, String dataFile)
			throws IOException, SAXException {
		String packagePath = ClassUtil.toFilePath(getClass().getPackage());
        Smooks smooks = new Smooks(packagePath + "/" + configFile);
        ExecutionContext executionContext = smooks.createExecutionContext();
    
        if(REPORT_EXECUTION) {
        	executionContext.setEventListener(new HtmlReportGenerator("target/report/" + dataFile + "-" + configFile + "/index.html"));
        }
        
    	String resource = StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream(dataFile)));
    	JavaResult result = new JavaResult();

        smooks.filterSource(executionContext, new StreamSource(new StringReader(resource)), result);

        @SuppressWarnings("unchecked")
        A[] as = (A[]) result.getBean("root");

        assertNotNull(as);
        assertEquals(2, as.length);

		A a1 = as[0];
		assertNotNull(a1.getBArray());
		assertEquals(3, a1.getBArray().length);
		assertEquals("b1", a1.getBArray()[0].getValue());
		assertEquals(a1, a1.getBArray()[0].getA());

		A a2 = as[1];
		assertNotNull(a2.getBArray());
		assertEquals(3, a2.getBArray().length);
		assertEquals("b4", a2.getBArray()[0].getValue());
		assertEquals(a2, a2.getBArray()[0].getA());


	}


}
