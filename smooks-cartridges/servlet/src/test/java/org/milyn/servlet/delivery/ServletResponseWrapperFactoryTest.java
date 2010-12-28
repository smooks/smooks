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

package org.milyn.servlet.delivery;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.MockExecutionContext;

import com.mockobjects.servlet.MockHttpServletResponse;

import junit.framework.TestCase;

public class ServletResponseWrapperFactoryTest extends TestCase {

	protected void setUp() throws Exception {
	}

	public void testCreateServletResponseWrapper() {
		SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("testrespwrapfactory", MyMockServletResponseWrapper.class.getName());
		MockExecutionContext request = new MockExecutionContext();
		MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();

		ServletResponseWrapper wrapper = ServletResponseWrapperFactory.createServletResponseWrapper(resourceConfig, request, mockServletResponse);
		assertNotNull(wrapper);
		if(!(wrapper instanceof MyMockServletResponseWrapper)) {
			fail("Expected MyMockServletResponseWrapper, got " + wrapper.getClass());
		}
	}
}
