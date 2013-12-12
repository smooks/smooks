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
package org.milyn.delivery;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class VisitorExceptionTest extends TestCase {

    protected void setUp() throws Exception {
        ExceptionVisitor.beforeException = null;
        ExceptionVisitor.afterException = null;
    }

    public void test_terminate_before() throws IOException, SAXException {
        ExceptionVisitor.beforeException = new SmooksException("Terminate Exception");
        test_exception("exception-config.xml", true);
        test_exception("exception-config-sax.xml", true);
    }

    public void test_terminate_after() throws IOException, SAXException {
        ExceptionVisitor.afterException = new SmooksException("Terminate Exception");
        test_exception("exception-config.xml", true);
        test_exception("exception-config-sax.xml", true);
    }

    public void test_no_terminate_before() throws IOException, SAXException {
        ExceptionVisitor.beforeException = new SmooksException("Terminate Exception");
        test_exception("no-exception-config.xml", false);
        test_exception("no-exception-config-sax.xml", false);
    }

    public void test_no_terminate_after() throws IOException, SAXException {
        ExceptionVisitor.afterException = new SmooksException("Terminate Exception");
        test_exception("no-exception-config.xml", false);
        test_exception("no-exception-config-sax.xml", false);
    }

    private void test_exception(String config, boolean expectException) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/delivery/" + config);

        if (expectException) {
            try {
                smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<doc/>")), null);
                fail("Expected SmooksException");
            } catch (SmooksException e) {
                assertEquals("Terminate Exception", e.getCause().getMessage());
            }
        } else {
            smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<doc/>")), null);
        }
    }
}
