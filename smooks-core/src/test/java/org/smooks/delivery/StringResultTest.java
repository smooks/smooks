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

package org.smooks.delivery;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;
import org.smooks.payload.StringResult;

/**
 * Unit test for StringResult
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class StringResultTest
{
	
	@Test
	public void tostring()
	{
		final String expectedString = "testing";
		StringResult stringResult = new StringResult();
		StringWriter writer = new StringWriter();
		writer.write( expectedString );
		
		stringResult.setWriter( writer );
		assertEquals( expectedString, stringResult.getWriter().toString() );
		assertEquals( expectedString, stringResult.toString() );
	}

}
