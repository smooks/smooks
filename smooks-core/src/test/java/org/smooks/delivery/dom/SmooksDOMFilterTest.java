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

package org.smooks.delivery.dom;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MockExecutionContext;
import org.smooks.io.StreamUtils;

/**
 * Unit test for SmooksDomFilter
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class SmooksDOMFilterTest
{
	private byte[] input;
	private ExecutionContext context;
	private SmooksDOMFilter domFilter;
	
	@Test
	public void doFilter_verify_that_flush_is_called() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult( baos );
		
		domFilter.doFilter( new StreamSource( new ByteArrayInputStream( input ) ), result );
		
		OutputStream outputStream = result.getOutputStream();
		assertTrue( outputStream instanceof ByteArrayOutputStream );
		
		byte[] byteArray = ((ByteArrayOutputStream)outputStream).toByteArray();
		assertTrue ( byteArray.length > 0 );
	}
	
	@Before
	public void setup() throws IOException
	{
		input = StreamUtils.readStream( getClass().getResourceAsStream( "testxml1.xml") );
		context = new MockExecutionContext();
		context.setContentEncoding( "UTF-8" );
		domFilter = new SmooksDOMFilter( context );
		
	}

}
