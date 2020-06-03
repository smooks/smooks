/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.io;

import static org.junit.Assert.*;
import org.junit.Test;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MockExecutionContext;
import org.smooks.delivery.Fragment;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Unit test for AbstractOutputStreamResouce 
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class AbstractOutputStreamResourceTest
{
	@Test
	public void getOutputStream () throws IOException
	{
		AbstractOutputStreamResource resource = new MockAbstractOutputStreamResource();
		MockExecutionContext executionContext = new MockExecutionContext();

        assertNull(getResource(resource, executionContext));
        resource.visitBefore( (Element)null, executionContext );
        assertNotNull(getResource(resource, executionContext));

		OutputStream outputStream = AbstractOutputStreamResource.getOutputStream( resource.getResourceName(), executionContext);
		assertNotNull( outputStream );
		assertTrue( outputStream instanceof ByteArrayOutputStream );

        // Should get an error now if we try get a writer to the same resource...
        try {
            AbstractOutputStreamResource.getOutputWriter( resource.getResourceName(), executionContext);
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("An OutputStream to the 'Mock' resource is already open.  Cannot open a Writer to this resource now!", e.getMessage());
        }

        resource.executeVisitLifecycleCleanup(new Fragment((Element)null), executionContext);

        // Should be unbound "after" and the stream should be closed...
        assertNull(getResource(resource, executionContext));
        assertTrue(MockAbstractOutputStreamResource.isClosed);
	}

    @Test
    public void getOutputWriter () throws IOException
    {
        AbstractOutputStreamResource resource = new MockAbstractOutputStreamResource();
        MockExecutionContext executionContext = new MockExecutionContext();

        assertNull(getResource(resource, executionContext));
        resource.visitBefore( (Element)null, executionContext );
        assertNotNull(getResource(resource, executionContext));

        Writer writer = AbstractOutputStreamResource.getOutputWriter(resource.getResourceName(), executionContext);
        assertNotNull( writer );
        assertTrue( writer instanceof OutputStreamWriter);

        // Should get an error now if we try get an OutputStream to the same resource...
        try {
            AbstractOutputStreamResource.getOutputStream( resource.getResourceName(), executionContext);
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("An Writer to the 'Mock' resource is already open.  Cannot open an OutputStream to this resource now!", e.getMessage());
        }

        resource.executeVisitLifecycleCleanup(new Fragment((Element)null), executionContext);

        // Should be unbound "after" and the stream should be closed...
        assertNull(getResource(resource, executionContext));
        assertTrue(MockAbstractOutputStreamResource.isClosed);
    }

    private Object getResource(AbstractOutputStreamResource resource, MockExecutionContext executionContext) {
        return executionContext.getAttribute( AbstractOutputStreamResource.RESOURCE_CONTEXT_KEY_PREFIX + resource.getResourceName());
    }

    /**
	 * Mock class for testing
	 */
	private static class MockAbstractOutputStreamResource extends AbstractOutputStreamResource
	{
        public static boolean isClosed = false;

		@Override
		public OutputStream getOutputStream( final ExecutionContext executionContext )
		{
            isClosed = false;
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    isClosed = true;
                    super.close();
                }
            };
		}

		@Override
		public String getResourceName()
		{
			return "Mock";
		}

        public Charset getWriterEncoding() {
            return Charset.forName("UTF-8");
        }
    }

}
