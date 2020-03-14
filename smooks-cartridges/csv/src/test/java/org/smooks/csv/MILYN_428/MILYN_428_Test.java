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
package org.smooks.csv.MILYN_428;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.csv.CSVHeaderValidationException;
import org.xml.sax.SAXException;

/**
 * 
 * @author Clemens Fuchslocher
 */
public class MILYN_428_Test {

	// No errors.
	@Test
	public void test01() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-01-config.xml"));
			smooks.filterSource(getSource("test-01-data.csv"));
		} catch (SmooksException exception) {
			fail(getStackTrace(exception));
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// No header.
	@Test
	public void test02() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-02-config.xml"));
			smooks.filterSource(getSource("test-02-data.csv"));
			fail();
		} catch (SmooksException exception) {
			assertException(
				exception,
				new String[] { "lastname", "firstname", "mail", "country", "city" },
				new String[] { "Mustermann", "Erika", "erika.mustermann@example.org", "Germany", "Berlin" },
				"expected == lastname,firstname,mail,country,city; found == Mustermann,Erika,erika.mustermann@example.org,Germany,Berlin"
			);
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// Empty header.
	@Test
	public void test03() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-03-config.xml"));
			smooks.filterSource(getSource("test-03-data.csv"));
			fail();
		} catch (SmooksException exception) {
			assertException(
				exception,
				new String[] { "lastname", "firstname", "mail", "country", "city" },
				new String[] { "" },
				"expected == lastname,firstname,mail,country,city; found == "
			);
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// Ignored fields.
	@Test
	public void test04() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-04-config.xml"));
			smooks.filterSource(getSource("test-04-data.csv"));
		} catch (SmooksException exception) {
			fail(getStackTrace(exception));
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// String manipulation functions.
	@Test
	public void test05() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-05-config.xml"));
			smooks.filterSource(getSource("test-05-data.csv"));
		} catch (SmooksException exception) {
			fail(getStackTrace(exception));
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// To few header fields.
	@Test
	public void test06() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-06-config.xml"));
			smooks.filterSource(getSource("test-06-data.csv"));
			fail();
		} catch (SmooksException exception) {
			assertException(
				exception,
				new String[] { "lastname", "firstname", "mail", "country", "city" },
				new String[] { "lastname", "firstname", "mail", "country" },
				"expected == lastname,firstname,mail,country,city; found == lastname,firstname,mail,country"
			);
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// To much header fields.
	@Test
	public void test07() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-07-config.xml"));
			smooks.filterSource(getSource("test-07-data.csv"));
			fail();
		} catch (SmooksException exception) {
			assertException(
				exception,
				new String[] { "lastname", "firstname", "mail", "country", "city" },
				new String[] { "lastname", "firstname", "mail", "country", "city", "street" },
				"expected == lastname,firstname,mail,country,city; found == lastname,firstname,mail,country,city,street"
			);
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// No header validation.
	@Test
	public void test08() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-08-config.xml"));
			smooks.filterSource(getSource("test-08-data.csv"));
		} catch (SmooksException exception) {
			fail(getStackTrace(exception));
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	// Disabled header validation.
	@Test
	public void test09() throws IOException, SAXException {
		Smooks smooks = null;
		try {
			smooks = new Smooks(getConfig("test-09-config.xml"));
			smooks.filterSource(getSource("test-09-data.csv"));
		} catch (SmooksException exception) {
			fail(getStackTrace(exception));
		} finally {
			if (smooks != null) {
				smooks.close();
			}
		}
	}

	private InputStream getConfig(final String file) throws FileNotFoundException {
		return getClass().getResourceAsStream(file);
	}

	private Source getSource(final String file) throws FileNotFoundException {
		return new StreamSource(getClass().getResourceAsStream(file));
	}

	public static String getStackTrace(final Throwable throwable) {
		Writer writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	private void assertException(final Exception exception, final String[] expected, final String[] found, final String message) {
		Throwable cause = exception.getCause();
		if (cause == null) {
			fail("cause == null");
		}

		if (!(cause instanceof CSVHeaderValidationException)) {
			fail("!(cause instanceof CsvHeaderValidationException)");
		}

		CSVHeaderValidationException validation = (CSVHeaderValidationException) cause;
		assertEquals(Arrays.asList(expected), validation.getExpected());
		assertEquals(Arrays.asList(found), validation.getFound());
		assertEquals(message, validation.getMessage());
	}
}
