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

package org.smooks.container.plugin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

/**
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class SourceFactoryTest
{
	private SourceFactory factory = SourceFactory.getInstance();
	@Test
	public void createStreamSourceStreamFromString()
	{
		Source source = factory.createSource( "testing" );
		assertNotNull( source );
		assertTrue( source instanceof StreamSource );
	}
	
	@Test
	public void getSourceByteArray()
	{
		Source source = factory.createSource( "test".getBytes() );
		assertNotNull( source );
		assertTrue( source instanceof StreamSource );
	}
	
	@Test
	public void getSourceReader()
	{
		Source source = factory.createSource( new StringReader( "testing" ));
		assertNotNull( source );
		assertTrue( source instanceof StreamSource );
	}
	
	@Test
	public void getSourceInputStream()
	{
		Source source = factory.createSource( new ByteArrayInputStream( "testing".getBytes() ) );
		assertNotNull( source );
		assertTrue( source instanceof StreamSource );
	}
	
}
