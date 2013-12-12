/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.delivery.Fragment;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Unit test for AbstractOutputStreamResouce
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class AbstractOutputStreamResourceTest {
    @Test
    public void getOutputStream() throws IOException {
        AbstractOutputStreamResource resource = new MockAbstractOutputStreamResource();
        MockExecutionContext executionContext = new MockExecutionContext();

        assertNull(getResource(resource, executionContext));
        resource.visitBefore((Element) null, executionContext);
        assertNotNull(getResource(resource, executionContext));

        OutputStream outputStream = AbstractOutputStreamResource.getOutputStream(resource.getResourceName(), executionContext);
        assertNotNull(outputStream);
        assertTrue(outputStream instanceof ByteArrayOutputStream);

        // Should get an error now if we try get a writer to the same resource...
        try {
            AbstractOutputStreamResource.getOutputWriter(resource.getResourceName(), executionContext);
            fail("Expected SmooksException");
        } catch (SmooksException e) {
            assertEquals("An OutputStream to the 'Mock' resource is already open.  Cannot open a Writer to this resource now!", e.getMessage());
        }

        resource.executeVisitLifecycleCleanup(new Fragment((Element) null), executionContext);

        // Should be unbound "after" and the stream should be closed...
        assertNull(getResource(resource, executionContext));
        assertTrue(MockAbstractOutputStreamResource.isClosed);
    }

    @Test
    public void getOutputWriter() throws IOException {
        AbstractOutputStreamResource resource = new MockAbstractOutputStreamResource();
        MockExecutionContext executionContext = new MockExecutionContext();

        assertNull(getResource(resource, executionContext));
        resource.visitBefore((Element) null, executionContext);
        assertNotNull(getResource(resource, executionContext));

        Writer writer = AbstractOutputStreamResource.getOutputWriter(resource.getResourceName(), executionContext);
        assertNotNull(writer);
        assertTrue(writer instanceof OutputStreamWriter);

        // Should get an error now if we try get an OutputStream to the same resource...
        try {
            AbstractOutputStreamResource.getOutputStream(resource.getResourceName(), executionContext);
            fail("Expected SmooksException");
        } catch (SmooksException e) {
            assertEquals("An Writer to the 'Mock' resource is already open.  Cannot open an OutputStream to this resource now!", e.getMessage());
        }

        resource.executeVisitLifecycleCleanup(new Fragment((Element) null), executionContext);

        // Should be unbound "after" and the stream should be closed...
        assertNull(getResource(resource, executionContext));
        assertTrue(MockAbstractOutputStreamResource.isClosed);
    }

    private Object getResource(AbstractOutputStreamResource resource, MockExecutionContext executionContext) {
        return executionContext.getAttribute(AbstractOutputStreamResource.RESOURCE_CONTEXT_KEY_PREFIX + resource.getResourceName());
    }

    /**
     * Mock class for testing
     */
    private static class MockAbstractOutputStreamResource extends AbstractOutputStreamResource {
        public static boolean isClosed = false;

        @Override
        public OutputStream getOutputStream(final ExecutionContext executionContext) {
            isClosed = false;
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    isClosed = true;
                    super.close();
                }
            };
        }

        @Override
        public String getResourceName() {
            return "Mock";
        }

        public Charset getWriterEncoding() {
            return Charset.forName("UTF-8");
        }
    }

}
