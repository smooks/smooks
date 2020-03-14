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
package org.smooks.routing.jms.message.creationstrategies;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;


/**
 * Unit test for StrategyFactory
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>				
 *
 */
@Test ( groups = "unit" )
public class StrategyFactoryTest
{
	@Test
	public void createTextMessageStrategy()
	{
		StrategyFactory instance = StrategyFactory.getInstance();
		MessageCreationStrategy createStrategy = instance.createStrategy( StrategyFactory.TEXT_MESSAGE );
		assertTrue( createStrategy instanceof TextMessageCreationStrategy );
	}
	
	@Test
	public void createObjectMessageStrategy()
	{
		StrategyFactory instance = StrategyFactory.getInstance();
		MessageCreationStrategy createStrategy = instance.createStrategy( StrategyFactory.OBJECT_MESSAGE );
		assertTrue( createStrategy instanceof ObjectMessageCreationStrategy );
	}

}
