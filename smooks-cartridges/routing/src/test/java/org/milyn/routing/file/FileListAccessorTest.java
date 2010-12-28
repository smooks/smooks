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

package org.milyn.routing.file;

import static org.testng.AssertJUnit.*;

import java.util.List;

import org.milyn.container.MockExecutionContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 	Unit test for FileListAccessor
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
@Test ( groups = "unit" )
public class FileListAccessorTest
{
	private MockExecutionContext execContext;

	@Test ( expectedExceptions = IllegalArgumentException.class )
	public void setFileNameNegative()
	{
		FileListAccessor.addFileName( null, execContext );
	}
	
	@Test
	public void addAndGetListFiles()
	{
		final String expectedFileName = "testing.txt";
		final String expectedFileName2 = "testing2.txt";
		FileListAccessor.addFileName( expectedFileName , execContext );
		FileListAccessor.addFileName( expectedFileName2 , execContext );
		FileListAccessor.addFileName( expectedFileName2 , execContext );
		List<String> list = FileListAccessor.getListFileNames( execContext );
		assertNotNull( list );
		assertTrue( list.size() == 2 );
	}
	
	@BeforeClass
	public void setup()
	{
		execContext = new MockExecutionContext();
	}

}
