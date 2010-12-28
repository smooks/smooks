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
package org.milyn.csv.MILYN_424;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.javabean.context.BeanContext;
import org.milyn.payload.StringResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Test for http://jira.codehaus.org/browse/MILYN-424.
 * <p/>
 * Test and fix contributed thanks to Clemens Fuchslocher.
 * 
 * @author Clemens Fuchslocher
 */
public class MILYN_424_Test extends TestCase {

	private static Logger logger = Logger.getLogger(MILYN_424_Test.class);

	public void test() throws IOException, SAXException {
		Smooks smooks = null;

		try {
			smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
			smooks.addVisitor(new SAXVisitAfter() {
				private Integer n = 0;

				public void visitAfter(final SAXElement element, final ExecutionContext execution) throws IOException {
					n++;

					logger.info("n == " + n);
					logger.info("element.getAttribute(\"number\") == " + element.getAttribute("number"));
					logger.info("element.getAttribute(\"truncated\") == " + element.getAttribute("truncated"));

					Attributes attributes = element.getAttributes();
					assertNotNull(attributes);

					logger.info("attributes.getIndex(\"number\") == " + attributes.getIndex("number"));
					logger.info("attributes.getIndex(\"truncated\") == " + attributes.getIndex("truncated"));

					for (int n = 0; n < attributes.getLength(); n++) {
						logger.info("attributes.getURI(" + n + ") == " + attributes.getURI(n));
						logger.info("attributes.getLocalName(" + n + ") == " + attributes.getLocalName(n));
						logger.info("attributes.getQName(" + n + ") == " + attributes.getQName(n));
						logger.info("attributes.getType(" + n + ") == " + attributes.getType(n));
						logger.info("attributes.getValue(" + n + ") == " + attributes.getValue(n));
					}

					BeanContext beans = execution.getBeanContext();
					assertNotNull(beans);

					Data data = (Data) beans.getBean("Data");
					assertNotNull(data);

					logger.info("data.getNumber() == " + data.getNumber());
					logger.info("data.getTruncated() == " + data.getTruncated());

					String number = element.getAttribute("number");
					assertNotNull("number == null", number);
					assertTrue("number.length() == 0", number.length() != 0);
					assertEquals(number, n.toString());

					String truncated = element.getAttribute("truncated");
					assertNotNull("truncated == null", truncated);
					if (n == 1 || n == 4 || n == 7) {
						assertEquals(truncated, "true");
					} else {
						assertEquals(truncated, "");
					}
				}
			}, "csv-record");

			StringResult result = new StringResult();
			smooks.filterSource(smooks.createExecutionContext(), getSource(), result);
			logger.info(result.toString());
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	private Source getSource() throws FileNotFoundException {
		return new StreamSource(getClass().getResourceAsStream("data.csv"));
	}

}
