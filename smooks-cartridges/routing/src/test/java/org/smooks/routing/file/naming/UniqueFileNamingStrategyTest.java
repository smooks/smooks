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

package org.smooks.routing.file.naming;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

/**
 * Test for class DefaultNamingStrategy
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
@Test ( groups = "unit" )
public class UniqueFileNamingStrategyTest
{
	private String pattern = "prefix-";

	UniqueFileNamingStrategy strategy = new UniqueFileNamingStrategy();

	@Test
	public void generateFileName()
	{
		String generateFileName = strategy.generateFileName( pattern, null );
		assertTrue( generateFileName.startsWith( pattern ) );
	}

}
