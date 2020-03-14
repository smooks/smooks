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
package org.smooks.db;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DatasourceCleanupTest {

    @Before
    public void setUp() throws Exception {
        MockDatasource.cleanupCallCount = 0;
    }

    @Test
    public void test_normal() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("normal-ds-lifecycle.xml"));

        // Cleanup should get called twice.  Once for the visitAfter event and once for the
        // executeExecutionLifecycleCleanup event...
        smooks.filterSource(new StringSource("<a></a>"));
        assertEquals(2, MockDatasource.cleanupCallCount);
        assertTrue(MockDatasource.committed);
    }

    @Test
    public void test_exception() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("exception-ds-lifecycle.xml"));

        try {
            smooks.filterSource(new StringSource("<a><b/><c/></a>"));
            fail("Expected exception...");
        } catch(SmooksException e) {
            // Expected
        }

        // Test that even after an exception is thrown, the DataSource cleanup takes place...
        // Cleanup should only get called once for the executeExecutionLifecycleCleanup event.
        // The visitAfter event doesn't call it because of the exception thrown by a nested
        // visitor...
        assertTrue(ExceptionVisitor.exceptionThrown);
        assertEquals(1, MockDatasource.cleanupCallCount);
        assertTrue(MockDatasource.rolledBack);
    }
}
