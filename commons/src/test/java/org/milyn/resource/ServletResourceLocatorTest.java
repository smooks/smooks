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

package org.milyn.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mockobjects.servlet.MockServletConfig;
import com.mockobjects.servlet.MockServletContext;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test that the getResource method tries to load based on the rules in the
 * following order: 1. First tries loading the resource based on the servlet
 * config (init-param) parameter if present. 2. Checks for a Servlet Context
 * parameter (context-param) and tries to load based on this if present. 3.
 * Tries based on the default supplied in the call.
 * 
 * @author tfennelly
 */
public class ServletResourceLocatorTest {

	private MockServletConfig getServletConfig() {
		MockServletConfig config = new MockServletConfig();
		config.setServletContext(new MyMockServletContext());
		return config;
	}

	@Test
	public void testConstructor() {
		try {
			new ServletResourceLocator(null, null);
			fail("no IllegalArgumentException on null 'config' param");
		} catch (IllegalArgumentException argE) {
		}
		new ServletResourceLocator(getServletConfig(), null);
	}

	@Test
	public void testGetResource_exceptions() {
		MockServletConfig config = getServletConfig();
		ServletResourceLocator servletLocator = new ServletResourceLocator(
				config, null);

		try {
			try {
				servletLocator.getResource(null, null);
				fail("no IllegalArgumentException on null 'paramName' param");
			} catch (IllegalArgumentException e) {
			}
			try {
				servletLocator.getResource("", null);
				fail("no IllegalArgumentException on empty 'paramName' param");
			} catch (IllegalArgumentException e) {
			}
			try {
				servletLocator.getResource(" ", null);
				fail("no IllegalArgumentException on whitespace 'paramName' param");
			} catch (IllegalArgumentException e) {
			}
			try {
				servletLocator.getResource("paramName", "");
				fail("no IllegalArgumentException on empty 'defaultLocation' param");
			} catch (IllegalArgumentException e) {
			}
			try {
				servletLocator.getResource("paramName", null);
				fail("no IllegalStateException on unconfigured servlet and no default");
			} catch (IllegalArgumentException e) {
				// This is a bit rough but better than not having it.
				assertEquals(
						"Resource [paramName] not specified in configuration, plus no default load location provided.",
						e.getMessage());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			fail(ioe.getMessage());
		}
	}

	/**
	 * Test that getResource loads based on the servlet config param -
	 * <init-param>
	 */
	@Test
	public void testGetResource_initparam() {
		MockServletConfig config = getServletConfig();
		MyMockServletContext context = ((MyMockServletContext) config
				.getServletContext());
		InputStream stream = new ByteArrayInputStream(new byte[] {});
		ServletResourceLocator servletLocator = new ServletResourceLocator(
				config, null);

		config.setInitParameter("paraX", "paraX-config");
		context.stream = stream;
		context.setInitParameter("paraX", "paraX-context");
		try {
			InputStream res = servletLocator.getResource("paraX", null);
			assertEquals(stream, res);
			// Make sure it came from the config
			assertEquals("paraX-config", context.paramName);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test that getResource loads based on the servlet context param -
	 * <context-param>
	 */
	@Test
	public void testGetResource_contextparam() {
		MockServletConfig config = getServletConfig();
		MyMockServletContext context = ((MyMockServletContext) config
				.getServletContext());
		InputStream stream = new ByteArrayInputStream(new byte[] {});
		ServletResourceLocator servletLocator = new ServletResourceLocator(
				config, null);

		// don't set the param in the servlet config
		context.stream = stream;
		context.setInitParameter("paraX", "paraX-context");
		try {
			InputStream res = servletLocator.getResource("paraX", null);
			assertEquals(stream, res);
			// Make sure it came from the context param
			assertEquals("paraX-context", context.paramName);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test that getResource loads based on the supplied default param
	 */
	@Test
	public void testGetResource_defaultparam() {
		MockServletConfig config = getServletConfig();
		MyMockServletContext context = ((MyMockServletContext) config
				.getServletContext());
		InputStream stream = new ByteArrayInputStream(new byte[] {});
		ServletResourceLocator servletLocator = new ServletResourceLocator(
				config, null);

		// don't set the param in the servlet config or context
		context.stream = stream;
		try {
			// Supply a default
			InputStream res = servletLocator.getResource("paraX",
					"paraX-default");
			assertEquals(stream, res);
			// Make sure it came from the context param
			assertEquals("paraX-default", context.paramName);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test that getResource loads from the external resource locator by making
	 * the resource stream load from the context fail.
	 */
	@Test
	public void testGetResource_fromURLResourceLocator() {
		MockServletConfig config = getServletConfig();
		MyMockServletContext context = ((MyMockServletContext) config
				.getServletContext());
		MockExternalResourceLocator resLocator = new MockExternalResourceLocator();
		ServletResourceLocator servletLocator = new ServletResourceLocator(
				config, resLocator);

		// don't set the param in the servlet config or context
		// and don't set the stream in the context forcing a load from the
		// external resource locator
		try {
			// Supply a default
			InputStream res = servletLocator.getResource("paraX",
					"paraX-default");
			// stream should be from the locator
			assertEquals(resLocator.stream, res);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	class MockExternalResourceLocator implements ExternalResourceLocator {

		InputStream stream = new ByteArrayInputStream(new byte[] {});

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.milyn.resource.ExternalResourceLocator#getResource(java.lang.String)
		 */
		public InputStream getResource(String uri)
				throws IllegalArgumentException, IOException {
			return stream;
		}

	}

	/**
	 * Mock context.
	 */
	class MyMockServletContext extends MockServletContext {
		public InputStream stream;

		public String paramName;

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
		 */
		public InputStream getResourceAsStream(String paramName) {
			this.paramName = paramName;
			if (paramName != null) {
				return stream;
			} else {
				return null;
			}
		}
	}
}
