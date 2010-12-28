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

package org.milyn.container.plugin;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.milyn.payload.JavaResult;

/**
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class ResultFactoryTest
{
	@Test
	public void createResultJava()
	{
		ResultFactory instance = ResultFactory.getInstance();
		Result result = instance.createResult( ResultType.JAVA );
		assertTrue( result instanceof JavaResult );
	}
	
	@Test
	public void createResultString()
	{
		ResultFactory instance = ResultFactory.getInstance();
		Result result = instance.createResult( ResultType.STRING );
		assertTrue( result instanceof StreamResult );
	}
	
	@Test
	public void createResultByteArray()
	{
		ResultFactory instance = ResultFactory.getInstance();
		Result result = instance.createResult( ResultType.BYTES );
		assertTrue( result instanceof StreamResult );
	}
	
	@Test
	public void createResultNoMap()
	{
		ResultFactory instance = ResultFactory.getInstance();
		Result result = instance.createResult( ResultType.NORESULT);
		assertNull( result );
	}
	
}
