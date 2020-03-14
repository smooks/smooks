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
package org.smooks.csv.MILYN_424;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.javabean.context.BeanContext;
import org.smooks.payload.StringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test for http://jira.codehaus.org/browse/MILYN-424.
 * <p/>
 * Test and fix contributed thanks to Clemens Fuchslocher.
 * 
 * @author Clemens Fuchslocher
 */
public class MILYN_424_Test {

	private static final Logger LOGGER = LoggerFactory.getLogger(MILYN_424_Test.class);

        @Test
	public void test() throws IOException, SAXException {
		Smooks smooks = null;

		try {
			smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
			smooks.addVisitor(new SAXVisitAfter() {
				private Integer n = 0;

				public void visitAfter(final SAXElement element, final ExecutionContext execution) throws IOException {
					n++;

					LOGGER.info("n == " + n);
					LOGGER.info("element.getAttribute(\"number\") == " + element.getAttribute("number"));
					LOGGER.info("element.getAttribute(\"truncated\") == " + element.getAttribute("truncated"));

					Attributes attributes = element.getAttributes();
					assertNotNull(attributes);

					LOGGER.info("attributes.getIndex(\"number\") == " + attributes.getIndex("number"));
					LOGGER.info("attributes.getIndex(\"truncated\") == " + attributes.getIndex("truncated"));

					for (int n = 0; n < attributes.getLength(); n++) {
						LOGGER.info("attributes.getURI(" + n + ") == " + attributes.getURI(n));
						LOGGER.info("attributes.getLocalName(" + n + ") == " + attributes.getLocalName(n));
						LOGGER.info("attributes.getQName(" + n + ") == " + attributes.getQName(n));
						LOGGER.info("attributes.getType(" + n + ") == " + attributes.getType(n));
						LOGGER.info("attributes.getValue(" + n + ") == " + attributes.getValue(n));
					}

					BeanContext beans = execution.getBeanContext();
					assertNotNull(beans);

					Data data = (Data) beans.getBean("Data");
					assertNotNull(data);

					LOGGER.info("data.getNumber() == " + data.getNumber());
					LOGGER.info("data.getTruncated() == " + data.getTruncated());

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
			LOGGER.info(result.toString());
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
