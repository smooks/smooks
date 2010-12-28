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

package org.milyn.routing.file.naming;

import static org.testng.AssertJUnit.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Unit test for TemplatedNamingStrategy
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
@Test ( groups = "unit" )
public class TemplatedNamingStrategyTest
{
	@Test
	public void test() throws NamingStrategyException, SAXException, IOException, ParserConfigurationException
	{
		TemplatedNamingStrategy strategy = new TemplatedNamingStrategy();
		Order order = new Order();
		order.setNr( 40 );
		String generateFileName = strategy.generateFileName( "OrderId-${nr}.txt", order );
		assertNotNull( generateFileName );
		assertEquals( "OrderId-40.txt", generateFileName );
	}
	
}
